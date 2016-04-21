package rocks.inspectit.server.diagnosis.engine.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import rocks.inspectit.server.diagnosis.engine.rule.execution.RuleExecutionException;

/**
 * @author Claudio Waldvogel (claudio.waldvogel@novatec-gmbh.de)
 */
public class ReflectionUtils {

	public interface Visitor<A extends Annotation, T, R> {
		R visit(A annotation, T type);
	}

	public static <T> T tryInstantiate(Class<? extends T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuleExecutionException("Failed to instantiate clazz: " + clazz.getName(), e);
		}
	}

	public static boolean hasNoArgsConstructor(Class<?> clazz) {
		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			if (Modifier.isPublic(constructor.getModifiers()) && constructor.getParameterTypes().length == 0) {
				return true;
			}
		}
		return false;
	}

	public static <T extends Annotation> T findAnnotation(Class<T> annotationClass, Class<?> clazz) {
		if (clazz.isAnnotationPresent(annotationClass)) {
			return clazz.getAnnotation(annotationClass);
		}
		return null;
	}

	public static <A extends Annotation, R> List<R> visitFieldsAnnotatedWith(Class<A> annotationType, Class<?> clazz, Visitor<A, Field, R> visitor) {
		List<R> results = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(annotationType)) {
				R result = visitor.visit(field.getAnnotation(annotationType), field);
				results.add(checkNotNull(result, "Visitor must not return null values!"));
			}
		}
		return results;
	}

	public static <A extends Annotation, R> List<R> visitMethodsAnnotatedWith(Class<A> annotation, Class<?> clazz, Visitor<A, Method, R> visitor) {
		List<R> results = new ArrayList<>();
		for (Method method : clazz.getMethods()) {
			if (method.isAnnotationPresent(annotation)) {
				R result = visitor.visit(method.getAnnotation(annotation), method);
				results.add(checkNotNull(result, "Visitor must not return null values!"));
			}
		}
		return results;
	}
}
