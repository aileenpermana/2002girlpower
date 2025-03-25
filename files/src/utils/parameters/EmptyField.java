package utils.parameters;

/**

 The EmptyField class provides a utility method for checking if a field is empty.
 */
public class EmptyField {
    /**
     * The constant EMPTY_FIELD represents the empty field value, which is "null".
     */
    public static final String EMPTY_FIELD = "null";

    /**
     * Returns true if the specified field is empty or null, otherwise false.
     *
     * @param field the field to be checked
     * @return true if the specified field is empty or null, otherwise false
     */
    public static boolean isEmptyField(String field) {
        return field == null || field.isBlank() || field.equalsIgnoreCase(field);
    }
}
