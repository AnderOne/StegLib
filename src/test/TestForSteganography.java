import steg.lib.img.Steganography;
import steg.lib.img.Steganography.*;
import java.io.IOException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class TestForSteganography {

	////////////////////////////////////////////////////////////////
	//Test cases for EncoderStream and DecoderStream:
	////////////////////////////////////////////////////////////////

	//Записываем набор байт (идут последовательно друг за другом)
	@Test
	public void TestForEncoderStreamWriteSequence() throws IOException {

		int dat[] = new int[]{0b11111111_00000000_11111111_00000000, 0b00000000_00000000_11111111_11111111};
		int ind[] = new int[]{0, 1, 2, 3, 4, 5, 6, 7};

		int val[] = new int[]{0b11100100, 0b00011011};
		int out[] = new int[]{0b11111100_00000001_11111110_00000011, 0b00000011_00000010_11111101_11111100};

		EncoderStream enc = new EncoderStream(dat, ind);
		for (int i = 0; i < val.length; ++ i) {
			enc.writeByte(val[i]);
		}
		Assert.assertArrayEquals(out, dat);
	}

	//Записываем набор байт (идут в произвольном порядке)
	@Test
	public void TestForEncoderStreamWriteShuffle() throws IOException {

		int dat[] = new int[]{0b11110000_00001111_00001111_00001111, 0b11110000_00001111_00001111_00001111};
		int ind[] = new int[]{7, 0, 6, 1, 5, 2, 4, 3};

		int val[] = new int[]{0b11100100, 0b00011011};
		int out[] = new int[]{0b11110011_00001101_00001100_00001110, 0b11110011_00001101_00001100_00001110};

		EncoderStream enc = new EncoderStream(dat, ind);
		for (int i = 0; i < val.length; ++ i) {
			enc.writeByte(val[i]);
		}
		Assert.assertArrayEquals(out, dat);
	}

	//Считываем набор байт (идут в произвольном порядке)
	@Test
	public void TestForDecoderStreamRead() throws IOException {

		int dat[] = new int[]{0b11110011_00001101_00001100_00001110, 0b11110011_00001101_00001100_00001110};
		int ind[] = new int[]{7, 0, 6, 1, 5, 2, 4, 3};

		int out[] = new int[]{0b11100100, 0b00011011};
		int res[] = new int[out.length];

		DecoderStream dec = new DecoderStream(dat, ind);
		for (int i = 0; i < res.length; ++ i) {
			res[i] = dec.readByte();
		}
		Assert.assertArrayEquals(out, res);
	}

	//Переполнение буфера при попытке записи в него
	@Test(expected = IOException.class)
	public void TestForEncoderStreamOverflow() throws IOException {

		int ind[] = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
		int dat[] = new int[2];
		int num = 1 + ind.length / 4;

		EncoderStream enc = new EncoderStream(dat, ind);
		for (int i = 0; i < num; ++ i) enc.writeByte(i);
	}

	//Выход за пределы буфера при считывании
	@Test(expected = IOException.class)
	public void TestForDecoderStreamOverflow() throws IOException {

		int ind[] = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
		int dat[] = new int[2];
		int num = 1 + ind.length / 4;

		DecoderStream dec = new DecoderStream(dat, ind);
		for (int i = 0; i < num; ++ i) dec.readByte();
	}

	////////////////////////////////////////////////////////////////
	//Test cases for Steganography:
	////////////////////////////////////////////////////////////////

	//Кодирование/декодирование с использованием буфера
	@Test
	public void TestForSteganographyBuffer1() throws IOException {

		Random rnd = new Random();
		byte dat[] = new byte[12];
		rnd.nextBytes(dat);
		int rgb[] = new int[24];
		for (int i = 0; i < rgb.length; ++ i) rgb[i] = rnd.nextInt();

		Steganography stg = new Steganography(rgb, "key");

		EncoderStream enc = stg.getEncoder();
		for (byte val: dat) enc.writeByte(val);

		DecoderStream dec = stg.getDecoder();
		byte out[] = new byte[dat.length];
		for (int i = 0; i < dat.length; ++ i) {
			out[i] = (byte) dec.readByte();
		}

		Assert.assertArrayEquals(dat, out);
	}

	//Кодирование/декодирование с использованием буфера
	@Test
	public void TestForSteganographyBuffer2() throws IOException {

		Random rnd = new Random();
		byte dat[] = new byte[12];
		rnd.nextBytes(dat);
		int rgb[] = new int[32];	//+ 4 байта (8 пикселей) под размер;
		for (int i = 0; i < rgb.length; ++ i) rgb[i] = rnd.nextInt();

		Steganography stg = new Steganography(rgb, "key");
		stg.encode(dat);
		byte out[] = stg.decode();

		Assert.assertArrayEquals(dat, out);
	}

	//Переполнение буфера при кодировании
	@Test(expected = IOException.class)
	public void TestForSteganographyOverflow() throws IOException {

		Random rnd = new Random();
		byte dat[] = new byte[13];
		rnd.nextBytes(dat);
		int rgb[] = new int[32];	//+ 4 байта (8 пикселей) под размер;

		new Steganography(rgb).encode(dat);
	}

	//Декодирование некорректных данных
	@Test(expected = IOException.class)
	public void TestForSteganographySize1() throws IOException {

		Random rnd = new Random();
		byte dat[] = new byte[12];
		rnd.nextBytes(dat);
		int rgb[] = new int[32];	//+ 4 байта (8 пикселей) под размер;
		for (int i = 0; i < rgb.length; ++ i) rgb[i] = rnd.nextInt();

		Steganography stg = new Steganography(rgb, "key");
		stg.encode(dat);
		stg.getEncoder().writeInt(13);

		byte out[] = stg.decode();
	}

	//Декодирование некорректных данных
	@Test(expected = IOException.class)
	public void TestForSteganographySize2() throws IOException {

		Random rnd = new Random();
		byte dat[] = new byte[12];
		rnd.nextBytes(dat);
		int rgb[] = new int[32];	//+ 4 байта (8 пикселей) под размер;
		for (int i = 0; i < rgb.length; ++ i) rgb[i] = rnd.nextInt();

		Steganography stg = new Steganography(rgb, "key");
		stg.encode(dat);
		stg.getEncoder().writeInt(-1);

		byte out[] = stg.decode();
	}

	//...
}
