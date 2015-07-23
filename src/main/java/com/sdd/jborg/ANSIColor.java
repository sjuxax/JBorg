package com.sdd.jborg;

public final class ANSIColor
{
	private final static String ESC = "\u001b[";
	private final static String SUFFIX = "m";
	private final static String BRIGHT = ESC + "1" + SUFFIX + ESC;

	public final static String RESET = ESC + "0" + SUFFIX;
	public final static String BLACK = ESC + "30" + SUFFIX;
	public final static String RED = ESC + "31" + SUFFIX;
	public final static String GREEN = ESC + "32" + SUFFIX;
	public final static String YELLOW = ESC + "33" + SUFFIX;
	public final static String BLUE = ESC + "34" + SUFFIX;
	public final static String MAGENTA = ESC + "35" + SUFFIX;
	public final static String CYAN = ESC + "36" + SUFFIX;
	public final static String WHITE = ESC + "37" + SUFFIX;

	public final static String GREY = BRIGHT + BLACK;
	public final static String BRIGHT_RED = BRIGHT + RED;
	public final static String BRIGHT_GREEN = BRIGHT + GREEN;
	public final static String BRIGHT_YELLOW = BRIGHT + YELLOW;
	public final static String BRIGHT_BLUE = BRIGHT + BLUE;
	public final static String BRIGHT_MAGENTA = BRIGHT + MAGENTA;
	public final static String BRIGHT_CYAN = BRIGHT + CYAN;
	public final static String BRIGHT_WHITE = BRIGHT + WHITE;
}
