package rocks.inspectit.server.diagnosis.engine.rule.factory;

import com.google.common.collect.Sets;
import rocks.inspectit.server.diagnosis.engine.rule.*;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.*;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleDefinitionException;
import rocks.inspectit.server.diagnosis.engine.rule.ConditionFailure;
import rocks.inspectit.server.diagnosis.engine.rule.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.*;

/**
 * @author Claudio Waldvogel
 */
public final class Rules {

	public static final String TRIGGER_RULE = "TRIGGER_RULE";

	/**
	 * Private constructor.
	 */
	private Rules() {
		// Must not be instantiated
	}

	public static RuleOutput triggerRuleOutput(Object input) {
		return new RuleOutput(TRIGGER_RULE, Tags.ROOT_TAG, new ArrayList<ConditionFailure>(), Collections.singleton(Tags.rootTag(input)));
	}

	public static Set<RuleDefinition> define(Class<?>... classes) {
		return define(Arrays.asList(classes));
	}

	public static Set<RuleDefinition> define(Collection<Class<?>> classes) {
		checkNotNull(classes, "Rule classes must not be null!");
		Set<RuleDefinition> ruleSet = Sets.newHashSet();
		for (Class<?> clazz : classes) {
			ruleSet.add(define(clazz));
		}
		return ruleSet;
	}

	public static RuleDefinition define(Class<?> clazz) {
		checkNotNull(clazz);
		if (!hasNoArgsConstructor(clazz)) {
			throw new RuleDefinitionException(clazz.getName() + " must define an empty default constructor.");
		}

		ActionMethod actionMethod = describeActionMethod(clazz);
		List<ConditionMethod> conditionMethods = describeConditionMethods(clazz);
		List<TagInjection> tagInjections = describeTagInjection(clazz);
		List<SessionVariableInjection> variableInjections = describeSessionParameterInjections(clazz);

		Rule rule = findAnnotation(Rule.class, clazz);
		String name = rule != null ? rule.name() : clazz.getName();
		String description = rule != null ? rule.description() : RuleDefinition.EMPTY_DESCRIPTION;
		FireCondition fireCondition = describeFireCondition(rule, tagInjections);

		return new RuleDefinition(name, description, clazz, fireCondition, conditionMethods, actionMethod, tagInjections, variableInjections);
	}

	// -------------------------------------------------------------
	// Methods: Descriptions
	// -------------------------------------------------------------

	/**
	 * @param rule
	 * @return
	 */
	public static FireCondition describeFireCondition(Rule rule, List<TagInjection> tagInjections) {
		if (rule != null && rule.fireCondition().length > 0) {
			return new FireCondition(Sets.newHashSet(Arrays.asList(rule.fireCondition())));
		} else {
			Set<String> requiredTypes = new HashSet<>();
			for (TagInjection injection : tagInjections) {
				requiredTypes.add(injection.getType());
			}
			return new FireCondition(requiredTypes);
		}
	}

	/**
	 * @param clazz
	 * @return
	 */
	public static List<SessionVariableInjection> describeSessionParameterInjections(Class<?> clazz) {
		return visitFieldsAnnotatedWith(SessionVariable.class, clazz, new Visitor<SessionVariable, Field, SessionVariableInjection>() {

			@Override
			public SessionVariableInjection visit(SessionVariable annotation, Field field) {
				return new SessionVariableInjection(annotation.name(), annotation.optional(), field);
			}

		});
	}

	public static List<TagInjection> describeTagInjection(Class<?> clazz) {
		List<TagInjection> tagInjections = visitFieldsAnnotatedWith(TagValue.class, clazz, new Visitor<TagValue, Field, TagInjection>() {
			@Override
			public TagInjection visit(TagValue annotation, Field field) {
				return new TagInjection(annotation.type(), field, annotation.injectionStrategy());
			}
		});

		return checkTagInjections(tagInjections, clazz);
	}

	public static ActionMethod describeActionMethod(Class<?> clazz) {
		List<ActionMethod> actionMethods = visitMethodsAnnotatedWith(Action.class, clazz, new Visitor<Action, Method, ActionMethod>() {
			@Override
			public ActionMethod visit(Action annotation, Method method) {
				return new ActionMethod(method, annotation.resultTag(), annotation.resultQuantity());
			}
		});

		return checkActionMethods(actionMethods);
	}

	public static List<ConditionMethod> describeConditionMethods(Class<?> clazz) {
		List<ConditionMethod> conditions = visitMethodsAnnotatedWith(Condition.class, clazz, new Visitor<Condition, Method, ConditionMethod>() {
			@Override
			public ConditionMethod visit(Condition annotation, Method method) {
				return new ConditionMethod(annotation.name(), annotation.hint(), method);
			}
		});
		return checkConditionMethods(conditions);
	}

	// -------------------------------------------------------------
	// Methods: Sanity checks
	// -------------------------------------------------------------

	public static List<TagInjection> checkTagInjections(List<TagInjection> tagInjections, Class<?> clazz) {
		// ensure that at least one value is inject, otherwise the rule will never fire
		if (tagInjections.size() < 1) {
			String msg = clazz.getName() + " must annotate at least one field with @Value. Otherwise the " + "rule will never fire and is useless.";
			throw new RuleDefinitionException(msg);
		}
		return tagInjections;
	}

	public static ActionMethod checkActionMethods(List<ActionMethod> actionMethods) {
		if (actionMethods.size() == 1) {
			ActionMethod action = actionMethods.get(0);
			Method method = action.getMethod();
			boolean valid = Modifier.isPublic(method.getModifiers());
			valid = valid && !method.getReturnType().equals(Void.class);
			valid = valid && (method.getParameterTypes().length == 0);
			if (!valid) {
				String msg = method.getDeclaringClass().getName() + " defines an invalid action method with name: " + method.getName();
				msg += "\nValid action methods are public with a non void return type and zero arguments (e.g. public" + " String action())";
				throw new RuleDefinitionException(msg);
			}
			//ensure proper return type in case of MULTIPLE outputQuantity
			if (action.getResultQuantity().equals(Action.Quantity.MULTIPLE)) {
				Class<?> returnType = action.getMethod().getReturnType();
				if (!returnType.isArray() && !Iterable.class.isAssignableFrom(returnType)) {
					String msg = method.getDeclaringClass().getName() + "defines an MULTIPLE outputQuantity, but return type is neither Array nor Collection.";
					throw new RuleDefinitionException(msg);
				}
			}
			return action;
		} else {
			throw new RuleDefinitionException("A rule must define exactly one method annotated with @Action. Otherwise the rule could never be exectued.");

		}
	}

	public static List<ConditionMethod> checkConditionMethods(List<ConditionMethod> methods) {
		for (ConditionMethod conditionMethod : methods) {
			Method method = conditionMethod.getMethod();

			boolean valid = Modifier.isPublic(method.getModifiers());
			valid = valid && (method.getReturnType().equals(boolean.class) || method.getReturnType().equals(Boolean.class));
			valid = valid && (method.getParameterTypes().length == 0);
			if (!valid) {
				String msg = method.getDeclaringClass().getName() + " defines an invalid condition method with name: " + method.getName();
				msg += "\nValid condition methods are public with a boolean return type and zero arguments (e.g. " + "public" + " boolean condition())";
				throw new RuleDefinitionException(msg);
			}
		}

		return methods;
	}

}