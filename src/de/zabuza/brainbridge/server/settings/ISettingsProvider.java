package de.zabuza.brainbridge.server.settings;

import java.util.Map;

/**
 * Interface for objects that provide values for settings.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public interface ISettingsProvider {

	/**
	 * Gets all settings of the provider as key-value paired map.
	 * 
	 * @return All settings of the provider as key-value paired map
	 */
	public Map<String, String> getAllSettings();

	/**
	 * Gets the value of a setting given by key.
	 * 
	 * @param key
	 *            The key of the setting
	 * @return The value of the setting
	 */
	public String getSetting(final String key);

	/**
	 * Sets the value for a setting given by key.
	 * 
	 * @param key
	 *            The key of the setting
	 * @param value
	 *            The value to set
	 */
	public void setSetting(final String key, final String value);

}
