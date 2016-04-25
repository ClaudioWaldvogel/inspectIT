package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkNotNull;
import static rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.findAnnotation;
import static rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.hasNoArgsConstructor;
import static rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.visitFieldsAnnotatedWith;
import static rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.visitMethodsAnnotatedWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import rocks.inspectit.server.diagnosis.engine.rule.api.Action;
import rocks.inspectit.server.diagnosis.engine.rule.api.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.api.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.api.SessionVariable;
import rocks.inspectit.server.diagnosis.engine.rule.api.TagValue;
import rocks.inspectit.server.diagnosis.engine.rule.definition.ActionMethod;
import rocks.inspectit.server.diagnosis.engine.rule.definition.ConditionMethod;
import rocks.inspectit.server.diagnosis.engine.rule.definition.FireCondition;
import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.definition.RuleDefinitionException;
import rocks.inspectit.server.diagnosis.engine.rule.definition.SessionVariableInjection;
import rocks.inspectit.server.diagnosis.engine.rule.definition.TagInjection;
import rocks.inspectit.server.diagnosis.engine.rule.execution.ConditionFailure;
import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.Visitor;

/**
 * @author Claudio Waldvogel
 */
public final class Rules {

	public static final String TRIGGER_RULE = "TRIGGER_RULE";

	public static final String EMPTY_RULE_DESCRIPTION = "Unknown";

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

		Rule rule = findAnnotation(Rule.class, clazz);
		String name = clazz.getName();
		String description = EMPTY_RULE_DESCRIPTION;
		FireCondition fireCondition = null;
		if (rule != null) {
			name = rule.name();
			description = rule.description();
			if (rule.fireCondition().length > 0) {
				fireCondition = new FireCondition(Sets.newHashSet(Arrays.asList(rule.fireCondition())));
			}
		}

		ActionMethod actionMethod = describeActionMethod(clazz);
		List<ConditionMethod> conditionMethods = describeConditionMethods(clazz);
		List<TagInjection> tagInjections = describeTagInjection(clazz);
		List<SessionVariableInjection> parameterInjections = describeSessionParameterInjections(clazz);

		// Ensure a fire condition if it was not provided by Rule annotation.
		// FireCondition can be extracted from the required injections
		if (fireCondition == null) {
			Set<String> requiredTypes = new HashSet<>();
			for (TagInjection injection : tagInjections) {
				requiredTypes.add(injection.getTagType());
			}
			fireCondition = new FireCondition(requiredTypes);
		}

		return new RuleDefinition(name, description, clazz, fireCondition, conditionMethods, actionMethod, tagInjections, parameterInjections);
	}

	// -------------------------------------------------------------
	// Methods: Descriptions
	// -------------------------------------------------------------

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
				return new TagInjection(annotation.tagType(), field, annotation.injectionType());
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
