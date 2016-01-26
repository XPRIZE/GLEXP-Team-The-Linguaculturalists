package com.linguaculturalists.phoenicia.locale;

import android.content.Context;

/**
 * Interface for Level pass checking
 */
public interface Requirement {

    /**
     * Check if this Requirement has been met.
     * @param context ApplicationContext needed for accessing the database
     * @return true if this requirement has been met, otherwise false
     */
    public boolean check(Context context);

}
