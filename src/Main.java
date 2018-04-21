import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

import steg.lib.img.BitMapWrapper;
import org.apache.commons.cli.*;

public class Main {

	public static void main(String args[]) throws IOException {

		Options options = new Options();

		Option arg_map = new Option("m", "map", true, "image container");
		arg_map.setRequired(true);
		options.addOption(arg_map);

		Option arg_inp = new Option("i", "inp", true, "input file path");
		arg_inp.setRequired(false);
		options.addOption(arg_inp);

		Option arg_out = new Option("o", "out", true, "output file");
		arg_out.setRequired(true);
		options.addOption(arg_out);

		Option arg_key = new Option("k", "key", true, "private key");
		arg_key.setRequired(false);
		options.addOption(arg_key);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		}
		catch (ParseException err) {
			System.out.println(err.getMessage());
			(new HelpFormatter()).printHelp("Main", options);
			System.exit(1);
			return;
		}
		String map = cmd.getOptionValue("map");
		String inp = cmd.getOptionValue("inp");
		String out = cmd.getOptionValue("out");
		String key = cmd.getOptionValue("key");

		BufferedImage img = ImageIO.read(new File(map));
		BitMapWrapper BW;
		if (key != null)
			BW = new BitMapWrapper(img, key);
		else {
			BW = new BitMapWrapper(img);
		}
		BufferedOutputStream buf_out;
		BufferedInputStream buf_inp;

		//Decoding:
		if (inp == null) {
			buf_out = new BufferedOutputStream(new FileOutputStream(out));
			BW.getCoder().decode(buf_out);
		}
		//Encoding:
		else {
			buf_inp = new BufferedInputStream(new FileInputStream(inp));
			BW.getCoder().setNoise();
			BW.getCoder().encode(buf_inp);
			img = BW.getImage();
			ImageIO.write(img, "PNG", new File(out));
		}
	}

}
