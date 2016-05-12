package rocks.inspectit.server.diagnosis.engine.rule.factory;

import com.google.common.collect.Sets;
import rocks.inspectit.server.diagnosis.engine.rule.*;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.*;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleDefinitionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils;
import rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.*;

/**
 * Utility method to work with rule classes. This class serves as static factory to create {@link RuleDefinition}s. So far it is not yet clear if this will stay, or a factory interface will be
 * provided to enable to possibility of replaceable {@link RuleDefinition} implementations.
 *
 * @author Claudio Waldvogel
 */
public final class Rules {

    /**
     * Private constructor.
     */
    private Rules() {
        throw new UnsupportedOperationException("Do not instantiate.");
    }

    /**
     * The name of the internal initial rule to kick of processing.
     */
    private static final String TRIGGER_RULE = "TRIGGER_RULE";

    /**
     * Creates the faked output of an internal rule to start process of other rules. If we wrap the input to be analyzed into a RuleOutput the entire rule processing works exactly the same way and
     * and the input can be access of type {@link Tags#ROOT_TAG}.
     *
     * @param input The input to be wrapped in output.
     * @return RuleOutput providing the input as value of {@link Tags#ROOT_TAG}.
     */
    public static RuleOutput triggerRuleOutput(Object input) {
        return new RuleOutput(TRIGGER_RULE, Tags.ROOT_TAG, new ArrayList<ConditionFailure>(), Collections.singleton(Tags.rootTag(input)));
    }

    /**
     * Methods transforms all given classes to {@link RuleDefinition}s.
     *
     * @param classes The classes to be transformed.
     * @return Set of {@link RuleDefinition}s.
     * @throws RuleDefinitionException if a class transformation fails.
     */
    public static Set<RuleDefinition> define(Class<?>... classes) {
        return define(Arrays.asList(classes));
    }

    /**
     * Methods transforms all given classes to {@link RuleDefinition}s.
     *
     * @param classes The classes to be transformed.
     * @return Set of {@link RuleDefinition}s.
     * @throws RuleDefinitionException if a class transformation fails.
     */
    public static Set<RuleDefinition> define(Collection<Class<?>> classes) {
        checkNotNull(classes, "Rule classes must not be null!");
        Set<RuleDefinition> ruleSet = Sets.newHashSet();
        for (Class<?> clazz : classes) {
            ruleSet.add(define(clazz));
        }
        return ruleSet;
    }


    /**
     * Methods transforms a class to a {@link RuleDefinition}.
     *
     * @param clazz The class to be transformed.
     * @return The corresponding {@link RuleDefinition}.
     * @throws RuleDefinitionException if transformation fails.
     */
    public static RuleDefinition define(final Class<?> clazz) {
        checkNotNull(clazz);
        Rule annotation = ReflectionUtils.findAnnotation(clazz, Rule.class);
        if (annotation == null) {
            throw new RuleDefinitionException(clazz.getName() + " must be annotated with @Rule annotation.");
        }
        if (!hasNoArgsConstructor(clazz)) {
            throw new RuleDefinitionException(clazz.getName() + " must define an empty default constructor.");
        }

        ActionMethod actionMethod = describeActionMethod(clazz);
        List<ConditionMethod> conditionMethods = describeConditionMethods(clazz);
        List<TagInjection> tagInjections = describeTagInjection(clazz);
        List<SessionVariableInjection> variableInjections = describeSessionParameterInjections(clazz);
        FireCondition fireCondition = describeFireCondition(annotation, tagInjections);

        return new RuleDefinition(annotation.name(), annotation.description(), clazz, fireCondition, conditionMethods, actionMethod, tagInjections, variableInjections);
    }

    // -------------------------------------------------------------
    // Methods: Descriptions
    // -------------------------------------------------------------

    /**
     * Utility method to create a {@link FireCondition} either from a {@link Rule} annotation or from {@link TagInjection}s.
     * The values defined in the annotation overrules the {@link TagInjection}s.
     *
     * @param rule The {@link Rule} annotation.
     * @return A new {@link FireCondition}
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
     * Extracts the {@link SessionVariableInjection}s from the given class by processing the {@link SessionVariable} annotations.
     *
     * @param clazz The class to be analyzed.
     * @return List of SessionVariableInjections
     */
    public static List<SessionVariableInjection> describeSessionParameterInjections(Class<?> clazz) {
        return visitFieldsAnnotatedWith(SessionVariable.class, clazz, new Visitor<SessionVariable, Field, SessionVariableInjection>() {
            @Override
            public SessionVariableInjection visit(SessionVariable annotation, Field field) {
                return new SessionVariableInjection(annotation.name(), annotation.optional(), field);
            }
        });
    }

    /**
     * Extracts the {@link TagInjection}s from the given class by processing the {@link TagInjection} annotations.
     *
     * @param clazz The class to be analyzed.
     * @return List of TagInjection
     */
    public static List<TagInjection> describeTagInjection(Class<?> clazz) {
        List<TagInjection> tagInjections = visitFieldsAnnotatedWith(TagValue.class, clazz, new Visitor<TagValue, Field, TagInjection>() {
            @Override
            public TagInjection visit(TagValue annotation, Field field) {
                return new TagInjection(annotation.type(), field, annotation.injectionStrategy());
            }
        });

        return checkTagInjections(tagInjections, clazz);
    }

    /**
     * Extracts the {@link Action} from the given class by processing the {@link Action} annotation.
     *
     * @param clazz The class to be analyzed.
     * @return List of ActionMethod
     */
    public static ActionMethod describeActionMethod(Class<?> clazz) {
        List<ActionMethod> actionMethods = visitMethodsAnnotatedWith(Action.class, clazz, new Visitor<Action, Method, ActionMethod>() {
            @Override
            public ActionMethod visit(Action annotation, Method method) {
                return new ActionMethod(method, annotation.resultTag(), annotation.resultQuantity());
            }
        });
        return checkActionMethods(actionMethods);
    }

    /**
     * Extracts the {@link ConditionMethod}s from the given class by processing the {@link Condition} annotation.
     *
     * @param clazz The class to be analyzed.
     * @return List of ConditionMethods
     */
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

    /**
     * Utility method to check the detected {@link TagInjection}s. Contract is that a class must defined at least one field annotated with {@link TagValue}.
     * Otherwise the rule could never fire, because no input could be determined.
     *
     * @param tagInjections The list of TagInjection to be checked.
     * @param clazz         The root class where TagInjection where extracted.
     * @return List of TagInjections
     */
    private static List<TagInjection> checkTagInjections(List<TagInjection> tagInjections, Class<?> clazz) {
        // ensure that at least one value is inject, otherwise the rule will never fire
        if (tagInjections.size() < 1) {
            String msg = clazz.getName() + " must annotate at least one field with @Value. Otherwise the " + "rule will never fire and is useless.";
            throw new RuleDefinitionException(msg);
        }
        return tagInjections;
    }

    /**
     * Utility method to check the detected {@link ActionMethod}s. Contract is that a class must defined exactly one method annotated with {@link Action}.
     * In addition the action method must be public, with zero arguments and void return type.
     * <pre>
     *     {@code
     *   	@literal @Action(resultTag = "SomeTag", resultQuantity = SINGLE)
     *   	public Object action(){
     *   	  return "A new Value";
     *   	}
     *     }
     * </pre>
     *
     * @param actionMethods The list of detected ActionMethods to be checked.
     * @return The valid ActionMethod
     */
    private static ActionMethod checkActionMethods(List<ActionMethod> actionMethods) {
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

    /**
     * Utility method to check the detected {@link ConditionMethod}s. Contract is that a class can define zero or more methods annotated with {@link Condition}.
     * In addition a condition method must be public, with zero arguments and boolean/Boolean return type.
     * <pre>
     * {@code
     *     @literal @Condition(name = "MyCondition", hint = "Some useful information")
     *     public boolean condition(){
     *         return true | false;
     *     }
     * }
     * </pre>
     *
     * @param conditionMethods The list of detected ConditionMethod to be checked.
     * @return The valid ActionMethod
     */
    private static List<ConditionMethod> checkConditionMethods(List<ConditionMethod> conditionMethods) {
        for (ConditionMethod conditionMethod : conditionMethods) {
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

        return conditionMethods;
    }

}
