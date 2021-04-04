package zipper;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import net.lingala.zip4j.model.ZipParameters;
import java.util.regex.Pattern;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.stream.Stream;
import java.nio.file.Path;
import java.util.regex.Matcher;
import net.lingala.zip4j.ZipFile;
import java.io.File;
import net.lingala.zip4j.exception.ZipException;
import java.io.UncheckedIOException;

import static javax.swing.SwingUtilities.invokeLater;
import static net.lingala.zip4j.model.enums.EncryptionMethod.ZIP_STANDARD;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static java.nio.file.Files.isRegularFile;

/**
 * PDF Files Zipper
 */
public final class Zipper extends JFrame
{
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Contains source directory
	 */
	private JTextField src;

	/**
	 * Directory chooser
	 */
	private JFileChooser fc;

	/**
	 * Amount of processed files
	 */
	private int count;

	/**
	 * Are there any errors?
	 */
	private boolean isCorrect;

	// Okienko z komunikatem
	private Alert arr;

	/**
	 * Zipping params
	 */
	private ZipParameters parameters;

	/**
	 * Password
	 */
	private char[] pass;

	/**
	 * Certificate pattern
	 */
	private final Pattern p;

	/**
	 * Zipper construction
	 */
	public Zipper()
	{
		super("Paczkomat");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 200);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
		URL imgUrl = getClass().getResource("/Zipper.png");

		if (imgUrl != null)
		{
			ImageIcon img = new ImageIcon(imgUrl);
			setIconImage(img.getImage());
		}

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		c.gridx = 0;
		c.gridy = 0;
		add(new JLabel("Lokalizacja z plikami:"), c);
		c.gridy = 1;
		src = new JTextField(40);
		add(src, c);
		c.gridx = 1;
		JButton s = new JButton("Przeglądaj");
		s.addActionListener((ActionEvent e) -> invokeLater(() -> getDir(src, s)));
		add(s, c);
		c.gridx = 0;
		c.gridy = 2;
		add(new JLabel("UWAGA! Program przemieli wszystkie pliki w podanym folderze!"), c);
		c.gridy = 3;
		JButton b = new JButton("Do dzieła!");

		b.addActionListener((ActionEvent e) -> invokeLater(() -> {
			b.setEnabled(false);
			zip(src.getText());
			b.setEnabled(true);
			isCorrect = true;
			count = 0;
		}));

		add(b, c);
		count = 0;
		isCorrect = true;
		setResizable(false);
		arr = new Alert();
		parameters = new ZipParameters();
		parameters.setEncryptFiles(true);
		parameters.setEncryptionMethod(ZIP_STANDARD);
		pass = new char[4];
		pass[0] = 's';
		pass[1] = 'w';
		pass[2] = 'r';
		pass[3] = 'n';
		p = Pattern.compile("(.+)_certyfikat.pdf");
		setVisible(true);
	}

	/**
	 * Directory choosing
	 * 
	 * @param target - text field which will receive a new directory
	 * @param parent - parent button for file chooser
	 */
	private void getDir(JTextField target, JButton parent)
	{
		int val = fc.showOpenDialog(parent);

		if (val == JFileChooser.APPROVE_OPTION)
		{
			target.setText(fc.getSelectedFile().getAbsolutePath());
		}
	}

	/**
	 * Files zipping
	 * 
	 * @param dir - working/source directory
	 */
	private void zip(final String dir)
	{
		final HashSet<String> set = new HashSet<String>();
		
		try (Stream<Path> paths = walk(get(dir), 1))
		{
			paths.forEach((Path filePath) -> {
				if (isRegularFile(filePath))
				{
					final Matcher mat = p.matcher(filePath.getFileName().toString());

					if (mat.matches())
					{
						set.add(mat.group(1));
					}
				}
			});

			for (String tmp : set)
			{
				ZipFile zip = new ZipFile(dir + '/' + tmp + ".zip");
				File cert = new File(dir + '/' + tmp + "_certyfikat.pdf");
				File list = new File(dir + '/' + tmp + "_list powitalny.pdf");
				zip.setPassword(pass);	        
				zip.addFile(cert, parameters);
				++count;

				if (list.exists())
				{
					zip.addFile(list, parameters);
					++count;
				}		
			}
		}
		catch (ZipException e)
		{
			alert("Wystąpił błąd i proces został zatrzymany. Spakowano " + count + " elementów.");
			isCorrect = false;
		}	
		catch (UncheckedIOException a)
		{
			alert("Błąd! Program nie ma dostępu do podanej lokalizacji. Spróbuj podać inną. (np. folder na pulpicie)");
			isCorrect = false;
		}
		catch (Exception e)
		{
			alert("Wystąpił błąd i proces został zatrzymany. Spakowano " + count + " elementów.");
			isCorrect = false;
		}

		if (isCorrect)
		{
			alert("Proces przebiegł bez zakłóceń. Spakowano " + count + " elementów.");
		}
	}

	/**
	 * Displays an alert window with a message
	 * 
	 * @param message - message to display
	 */
	private void alert(String message)
	{
		invokeLater(() -> arr.show(message));
	}
}
