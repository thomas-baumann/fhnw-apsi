package lab1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TextGenerator {

	private String[][] text;
	private int nr = -1;

	public void loadTextFile(String fileName) throws IOException {
		BufferedReader lineReader = new BufferedReader(new FileReader(fileName));
		int lines = 0;
		while (lineReader.readLine() != null) {
			lines++;
		}
		lineReader.close();

		this.text = new String[lines][2];

		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line;
		int lineNr = 0;
		line = reader.readLine();
		while (line != null) {
			int pos = line.indexOf("|");
			if (pos >= 0) {
				this.text[lineNr][0] = line.substring(0, pos);
				this.text[lineNr][1] = line.substring(pos + 1, line.length());
			} else {
				this.text[lineNr][0] = line;
				this.text[lineNr][1] = line;
			}
			line = reader.readLine();
			lineNr++;
		}
		reader.close();
	}

	public String getNextText() {
		return this.getText(this.nr--);
	}

	public String getText(int nr) {
		String binary = Integer.toBinaryString(nr);

		StringBuilder st = new StringBuilder();

		int lengthBinary = binary.length();
		int lengthZeros = 32 - lengthBinary;

		for (int i = 0; i < lengthZeros; i++) {
			st.append(this.text[i][0]);
		}
		for (int i = 0; i < lengthBinary; i++) {
			st.append(this.text[i + lengthZeros][(binary.charAt(i)) - 48]);
		}
		return st.toString();
	}
}