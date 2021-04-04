package zipper;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * Program's starting class
 */
public final class Main
{
	/**
	 * Program's starting point
	 * 
	 * @param args - program's arguments
	 */
	public static void main(String[] args)
	{
		invokeLater(() -> {
			new Zipper();
		});
	}
	
	/**
	 * No instances allowed
	 */
	private Main() {}
}
