package mp;

/**
 * Заглушка для артефактов декомпиляции kotlin.Result (inline-класс).
 *
 * Тонкость: когда Result передаётся через generic-колбэк, Kotlin БОКСИТ его
 * в объект kotlin.Result (внутри поле value). Поэтому сначала разворачиваем
 * бокс, иначе получаем "kotlin.Result cannot be cast to java.util.List".
 */
public final class MpResult {

    private static Object unbox(Object v) {
        if (v == null) return null;
        if (v.getClass().getName().equals("kotlin.Result")) {
            try {
                java.lang.reflect.Field f = v.getClass().getDeclaredField("value");
                f.setAccessible(true);
                return f.get(v);
            } catch (java.lang.Throwable ignored) {
                return v;
            }
        }
        return v;
    }

    public static Object constructorimpl(Object v) {
        return v;
    }

    public static Object m316constructorimpl(Object v) {
        return v;
    }

    public static boolean m323isSuccessimpl(Object v) {
        return !(unbox(v) instanceof kotlin.Result.Failure);
    }

    public static boolean m322isFailureimpl(Object v) {
        return unbox(v) instanceof kotlin.Result.Failure;
    }

    public static Object m319exceptionOrNullimpl(Object v) {
        Object u = unbox(v);
        if (u instanceof kotlin.Result.Failure) {
            return ((kotlin.Result.Failure) u).exception;
        }
        return null;
    }

    public static Object getValue(Object v) {
        return unbox(v);
    }
}
