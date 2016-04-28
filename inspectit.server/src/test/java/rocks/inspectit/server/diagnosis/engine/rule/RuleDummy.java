/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule;

import rocks.inspectit.server.diagnosis.engine.tag.Tag;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Claudio Waldvogel
 */
public class RuleDummy {


    public String tagStringValueField;
    public Tag tagAsTagField;
    public Integer sessionIntVariable;


    public static Method actionMethod() {
        return wrap(new Callable<Method>() {
            @Override
            public Method call() throws Exception {
                return RuleDummy.class.getDeclaredMethod("action");
            }
        });
    }

    public static Field tagStringValueField() {
        return wrap(new Callable<Field>() {
            @Override
            public Field call() throws Exception {
                return RuleDummy.class.getDeclaredField("tagStringValueField");
            }
        });
    }

    public static Field tagAsTagField() {
        return wrap(new Callable<Field>() {
            @Override
            public Field call() throws Exception {
                return RuleDummy.class.getDeclaredField("tagAsTagField");
            }
        });
    }

    public static Field sessionVariableIntField() {
        return wrap(new Callable<Field>() {
            @Override
            public Field call() throws Exception {
                return RuleDummy.class.getDeclaredField("sessionIntVariable");
            }
        });
    }

    private static <T> T wrap(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean failConidtion() {
        return false;
    }

    public boolean successCondiction() {
        return true;
    }

    public Object action() {
        return "action";
    }


}
