package us.talabrek.ultimateskyblock.util;



/**
 * Convenience util for supporting static imports.
 */
public enum I18nUtil {
	I18nFactory;
    @SuppressWarnings("unused")
	private static final I18nUtil i18n = I18nFactory.getI18n(I18nUtil.class);

    public static String tr(String s) {
       //return I18nUtil.tr(s);
        return s;
    }
    private I18nUtil getI18n(Class<I18nUtil> class1) {
		// TODO Auto-generated method stub
		return null;
	}
	public static String tr(String s, Object... args) {
        //return I18nUtil.tr(s, args);
        return s;
    }
}
