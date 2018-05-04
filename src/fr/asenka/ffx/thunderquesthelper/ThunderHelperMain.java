package fr.asenka.ffx.thunderquesthelper;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

import javax.management.RuntimeErrorException;

/**
 * <p>
 * If you like Final Fantasy X, if you have it on PC with the latest HD remix,
 * if you want to have the Lulu best weapon and if you are lazy then this should
 * be helpful, hopefully !
 * </p>
 * 
 * <p>
 * By Launching this small Java program, you can move around the Thunder plain
 * in Final Fantasy X and it will take care of avoiding the lightnings by
 * pressing the key you need at the time you need. Just make sure that the key
 * pressed by this program is the key you configured in game.
 * </p>
 * 
 * 
 * @author asenka
 * @see Robot
 *
 */
public class ThunderHelperMain {

	private static final DecimalFormat DF = new DecimalFormat(".##");

	/**
	 * <p>
	 * Use <code>"--delay=[int_value]"</code> to change the time between to screen
	 * brightness check (should be small).
	 * </p>
	 * Default value is {@link #DEFAULT_SLEEP_TIME}
	 */
	private static final String PARAM_DELAY_BETWEEN_CHECK = "--delay=";

	/**
	 * <p>
	 * Use <code>"--delay-after-hit=[int_value]"</code> to change how much time the
	 * program sleeps after a hit.
	 * </p>
	 * Default value is {@link #DEFAULT_SLEEP_TIME_AFTER_HIT}
	 */
	private static final String PARAM_DELAY_AFTER_HIT = "--delay-after-hit=";

	/**
	 * <p>
	 * Use <code>"--input-lag=[int_value]"</code> to the input lag before pressing
	 * the key.
	 * </p>
	 * Default value is {@link #DEFAULT_INPUT_LAG}
	 */
	private static final String PARAM_INPUT_LAG = "--input-lag=";

	/**
	 * <p>
	 * Use <code>"--brightness-threshold=[int_value]"</code> to change the level of
	 * brightness need to trigger the key press.
	 * </p>
	 * <p>
	 * Default value is {@link #DEFAULT_BRIGTNESS_THRESHOLD}
	 */
	private static final String PARAM_BRIGHTNESS_THRESHOLD = "--brightness-threshold=";

	private static final String PARAM_PRESS_TIMEOUT = "--press-timeout=";

	/**
	 * <p>
	 * Use <code>"--key=[key]"</code> to change the key pressed
	 * </p>
	 * Default value is SPACE
	 */
	private static final String PARAM_KEY = "--key=";

	/**
	 * <p>
	 * Use <code>"--delay-after-hit=[int_value]"</code> to change how much time the
	 * program sleeps after a hit.
	 * </p>
	 * <p>
	 * Default value is {@link #DEFAULT_SLEEP_TIME_AFTER_HIT}
	 */
	private static final String PARAM_MAX_HIT = "--max-hit=";

	private static final int DEFAULT_SLEEP_TIME = 50;
	private static final int DEFAULT_SLEEP_TIME_AFTER_HIT = 3000;
	private static final int DEFAULT_INPUT_LAG = 100;
	private static final int DEFAULT_MAX_HIT = 200;
	private static final int DEFAULT_PRESS_TIMEOUT = 80;
	private static final int DEFAULT_KEY = KeyEvent.VK_SPACE;
	private static final double DEFAULT_BRIGTNESS_THRESHOLD = 160.0;

	private static final Robot ROBOT = getRobot();

	/**
	 * Entry point of the program
	 * 
	 * @param args
	 *            the command line arguments (user arguments)
	 */
	public static void main(String[] args) {

		// Read the user params from command line arguments
		final int paramDelayBetweenCheck = getCommandParam(args, PARAM_DELAY_BETWEEN_CHECK);
		final int paramDelayAfterHit = getCommandParam(args, PARAM_DELAY_AFTER_HIT);
		final int paramInputLag = getCommandParam(args, PARAM_INPUT_LAG);
		final int paramBrightnessThreshold = getCommandParam(args, PARAM_BRIGHTNESS_THRESHOLD);
		final int paramKey = getCommandParam(args, PARAM_KEY);
		final int paramMaxHit = getCommandParam(args, PARAM_MAX_HIT);
		final int paramPressTimeout = getCommandParam(args, PARAM_PRESS_TIMEOUT);
		final int sleepTime = paramDelayBetweenCheck > 0 ? paramDelayBetweenCheck : DEFAULT_SLEEP_TIME;
		final int sleepTimeAfterHit = paramDelayAfterHit > 0 ? paramDelayAfterHit : DEFAULT_SLEEP_TIME_AFTER_HIT;
		final int inputLag = paramInputLag > 0 ? paramInputLag : DEFAULT_INPUT_LAG;
		final double brightnessThreshold = paramBrightnessThreshold > 0 ? (double) paramBrightnessThreshold
				: DEFAULT_BRIGTNESS_THRESHOLD;
		final int key = paramKey > 0 ? KeyEvent.getExtendedKeyCodeForChar(paramKey) : DEFAULT_KEY;
		final int maxHit = paramMaxHit > 0 ? paramMaxHit : DEFAULT_MAX_HIT;
		final int pressTimeout = paramPressTimeout > 0 ? paramPressTimeout : DEFAULT_PRESS_TIMEOUT;

		// Display summary of current instance values
		System.out.println("----------------------------------------");
		System.out.println("Launch Thunder Helper with parameters :");
		System.out.println(PARAM_DELAY_BETWEEN_CHECK + sleepTime);
		System.out.println(PARAM_DELAY_AFTER_HIT + sleepTimeAfterHit);
		System.out.println(PARAM_INPUT_LAG + inputLag);
		System.out.println(PARAM_BRIGHTNESS_THRESHOLD + brightnessThreshold);
		System.out.println(PARAM_KEY + KeyEvent.getKeyText(key));
		System.out.println(PARAM_MAX_HIT + maxHit);
		System.out.println("Press CTRL + C to stop.");
		System.out.println("----------------------------------------");

		// Find the point to control on the screen (should be in the center)
		final Point control = getControlPoint();

		// MAIN LOOP
		int hitCount = 0;
		while (hitCount < maxHit) {

			// Calculate the current brightness of the controled point on the screen
			double brightness = getBrightness(ROBOT.getPixelColor(control.x, control.y));

			// If the brightness is superor than the threshold...
			if (brightness >= brightnessThreshold) {
				hitCount++;
				System.out.println("THUNDER ! (" + DF.format(brightness) + ") \t hit counter=" + hitCount);
				System.out.print("Press " + KeyEvent.getKeyText(key) + " ... ");
				pressKey(key, inputLag, pressTimeout);
				System.out.print("Sleep...");
				sleep(sleepTimeAfterHit);
				System.out.println("Wake Up!");
			} else {
				sleep(sleepTime);
			}
		}
	}

	/**
	 * Calculate the brightness of a color.
	 * 
	 * http://www.nbdtech.com/Blog/archive/2008/04/27/Calculating-the-Perceived-Brightness-of-a-Color.aspx
	 * https://en.wikipedia.org/wiki/Relative_luminance
	 * 
	 * @param color
	 *            the color
	 * @return a double value between 0. (darkest) and 255. (brightest)
	 */
	private static final double getBrightness(final Color color) {

		final double red = color.getRed();
		final double green = color.getGreen();
		final double blue = color.getBlue();
		return Math.sqrt((red * red * 0.241) + (green * green * 0.691) + (blue * blue * 0.068));
	}

	/**
	 * The method must returns the point to control on the screen. Depending on the
	 * brightness of the color at this point we can trigger or not specific events
	 * 
	 * @return the point in the middle of the screen
	 */
	private static final Point getControlPoint() {

		Rectangle screenArea = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		return new Point((int) screenArea.getCenterX(), (int) screenArea.getCenterY());
	}

	/**
	 * Press the requested key with a input lag
	 * 
	 * @param key
	 *            the key to press
	 * @param inputLag
	 *            how long to wait before pressing
	 * @param pressTimeout
	 */
	private static final void pressKey(int key, int inputLag, int pressTimeout) {

		sleep(inputLag);
		ROBOT.keyPress(key);
		sleep(pressTimeout);
		ROBOT.keyRelease(key); // The key must be released after it has been pressed
	}

	/**
	 * Utility method used to make this program sleep for a certain time
	 * 
	 * @param millis
	 *            the tome to sleep in milliseconds
	 * @see Thread#sleep(long)
	 */
	private static final void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeErrorException(new Error(e));
		}
	}

	/**
	 * Initialise the awt Robot used to analyse the screen
	 * 
	 * @return the awt Robot
	 * @see Robot
	 * @throws RuntimeErrorException
	 *             if the robot cannot be initialized
	 */
	private static final Robot getRobot() {

		try {
			return new Robot();
		} catch (AWTException e) {
			throw new RuntimeErrorException(new Error(e));
		}
	}

	/**
	 * Analyse the command line arguments and return a value if the requested param
	 * is in the list of user arguments
	 * 
	 * @param args
	 *            the command line arguments
	 * @param argName
	 *            the name of the requested param
	 * @return an integer value of the requested param or -1 if not found
	 * 
	 */
	private static final int getCommandParam(String[] args, String argName) {

		for (String arg : args) {

			if (arg.startsWith(argName)) {

				String value = arg.replace(argName, "");
				return PARAM_KEY.equals(argName) ? value.charAt(0) : Integer.valueOf(value);
			}
		}
		return -1;
	}
}
