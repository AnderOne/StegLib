package steg.lib.img;

import java.security.SecureRandom;
import java.util.Random;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * The main class which includes implementation of steganography algorithms to encode/decode of arbitrary data
 * into/from RGBA-buffer.
 */
public class Steganography {

	/**
	 * Encoder stream class to write hidden data into RGBA-buffer.
	 */
	public static class EncoderStream extends BaseStream {

		/**
		 * Creates a new output stream to write hidden data into RGBA-buffer.
		 *
		 * @param buf output RGBA-buffer which will contain hidden data.
		 * @param ind array of indices determining order of hidden bytes in the output buffer.
		 */
		public EncoderStream(int buf[], int ind[]) { super(buf, ind); }

		/**
		 * Writes one hidden byte into RGBA-buffer and shifts position.
		 *
		 * @param c value of byte [0..255] to write into buffer.
		 * @throws IOException if there is overflow of the output buffer.
		 */
		public void writeByte(int c) throws IOException {

			if (pos > ind.length - 4) throw new IOException("Overflow of output buffer!");

			for (int i = 6; i >= 0; i -= 2) {
				int p = ind[pos ++];
				int s = 8 * (p % 4);
				p /= 4;
				buf[p] = (buf[p] & (~ (0x03 << s))) | (((c >>> i) & 0x03) << s);
			}
		}

		/**
		 * Writes one integer into RGBA-buffer using big-endian format.
		 *
		 * @param val value of integer to write into buffer.
		 * @throws IOException if there is overflow of the output buffer.
		 */
		public void writeInt(int val) throws IOException {
			writeByte((val >>> 24) & 0xFF);
			writeByte((val >>> 16) & 0xFF);
			writeByte((val >>> 8) & 0xFF);
			writeByte((val) & 0xFF);
		}
	}

	/**
	 * Decoder stream class to read hidden data from RGBA-buffer.
	 */
	public static class DecoderStream extends BaseStream {

		/**
		 * Creates a new input stream to read hidden data from RGBA-buffer.
		 *
		 * @param buf input RGBA-buffer which contains hidden data.
		 * @param ind array of indices determining order of hidden bytes in the buffer.
		 */
		public DecoderStream(int buf[], int ind[]) { super(buf, ind); }

		/**
		 * Reads one hidden byte from RGBA-buffer and shifts position.
		 *
		 * @return Value of the current byte [0..255].
		 * @throws IOException if end of the input buffer was reached.
		 */
		public int readByte() throws IOException {

			if (pos > ind.length - 4) throw new IOException("End of input buffer!");
			int c = 0;
			for (int i = 0; i < 4; ++ i) {
				int p = ind[pos ++];
				int s = 8 * (p % 4);
				p /= 4;
				c = (c << 2) | ((buf[p] >>> s) & 0x03);
			}
			return c;
		}

		/**
		 * Reads one int (4 bytes) from RGBA-buffer and shifts position.
		 *
		 * @return Value of the current integer.
		 * @throws IOException if end of the input buffer was reached.
		 */
		public int readInt() throws IOException {
			int val = 0;
			for (int i = 0; i < 4; ++ i) {
				val = (val << 8) | readByte();
			}
			return val;
		}
	}

	/**
	 * Abstract base class of stream based on RGBA-buffer.
	 */
	public static abstract class BaseStream {

		/**
		 * Set of the current position (index of byte) in RGBA-buffer.
		 *
		 * @param p target position from start of the buffer.
		 * @throws IOException if position out of bounds.
		 */
		public void seek(int p) throws IOException {
			if ((pos = 4 * p) >= ind.length) {
				throw new IOException("Going to out the buffer!");
			}
			if (p < 0) {
				throw new IOException("Incorrect offset!");
			}
		}

		BaseStream(int buf[], int ind[]) {
			super();
			this.buf = buf; this.ind = ind;
		}

		int buf[], ind[];
		int pos = 0;
	}

	/**
	 * Creates a new steganography object using key to randomly shuffle parts of hidden data in the bitmap.
	 *
	 * @param buf RGBA-buffer which will be used to hide data.
	 * @param key private key to encode/decode algorithms.
	 */
	public Steganography(int buf[], String key) { dat = buf.clone(); reset(key); }

	/**
	 * Creates a new steganography object. It doesn't use shuffling (all parts are stored sequentially).
	 *
	 * @param buf RGBA-buffer which will be used to hide data.
	 */
	public Steganography(int buf[]) { dat = buf.clone(); reset(""); }

	/**
	 * Resets private key determining location of a hidden data in the RGBA-buffer.
	 *
	 * @param key private key to encoder/decoder algorithms.
	 */
	public void reset(String key) {

		if ((ind == null) || (ind.length < 2 * dat.length)) ind = new int[2 * dat.length];
		//We use only R and B components:
		for (int i = 0; i < dat.length; ++ i) ind[2 * i + 1] = (ind[2 * i] = 4 * i) + 2;

		if (key.length() == 0) return;

		int h = 0;
		for (int i = 0; i < key.length(); ++ i) h = Math.abs(h + key.charAt(i)) % MAGIC;
		for (int i = 0; i < ind.length; ++ i) {
			h = Math.abs(h * key.charAt(i % key.length()) + MAGIC) % ind.length;
			int t  = ind[h];
			ind[h] = ind[i];
			ind[i] = t;
		}
	}

	//Private fields:
	private static final int MAGIC = ((1 << 17) - 1);	//Magic prime number!

	int dat[];	//RGBA-buffer:
	int ind[];	//Indices;

	/**
	 *  Makes of random noise to hide which pixels were used to store data.
	 */
	public void setNoise() {
		Random rnd = new Random(); rnd.setSeed((new SecureRandom()).nextLong());
		for (int i = 1; i < dat.length; i += 2) dat[i] ^= rnd.nextInt(4);
	}

	/**
	 * Gets a new encoder stream based on RGBA-buffer.
	 *
	 * @return Encoder stream object.
	 */
	public EncoderStream getEncoder() { return new EncoderStream(dat, ind); }

	/**
	 * Gets a new decoder stream based on RGBA-buffer.
	 *
	 * @return Decoder stream object.
	 */
	public DecoderStream getDecoder()  { return new DecoderStream(dat, ind); }

	/**
	 * Encodes input stream to RGBA-buffer by steganography algorithm.
	 *
	 * @param  inp input stream which must be encoded.
	 * @throws IOException if there is fail to encode.
	 */
	public final void encode(InputStream inp) throws IOException {

		EncoderStream enc = new EncoderStream(dat, ind);
		int num = 0;
		enc.seek(4);	//Пропускаем первые 4 байта, отведенные под размер записанных данных;
		for (int ch; (ch = inp.read()) != -1; ++ num) {
			enc.writeByte(ch);
		}
		//Возвращаемся в начало и записываем полученный размер:
		enc.seek(0);
		enc.writeInt(num);
	}

	/**
	 * Encodes input buffer to RGBA-buffer by steganography algorithm.
	 *
	 * @param  inp input buffer which must be encoded.
	 * @throws IOException if there is fail to encode.
	 */
	public final void encode(byte[] inp) throws IOException {

		EncoderStream enc = new EncoderStream(dat, ind);
		int num = inp.length;
		//Записываем размер:
		enc.writeInt(num);
		for (int i = 0; i < num; ++ i) enc.writeByte(inp[i] & 0xFF);
	}

	/**
	 * Decodes hidden data from RGBA-buffer by steganography algorithm.
	 *
	 * @param  out output stream to write decoded data.
	 * @throws IOException if there is fail to decode.
	 */
	public final void decode(OutputStream out) throws IOException {

		DecoderStream dec = new DecoderStream(dat, ind);
		int num = dec.readInt();
		while ((num --) != 0) out.write(dec.readByte());

		out.flush();
	}

	/**
	 * Decodes hidden data from RGBA-buffer by steganography algorithm.
	 *
	 * @return Output buffer with decoded data.
	 * @throws IOException if there is fail to decode.
	 */
	public final byte[] decode() throws IOException {

		DecoderStream dec = new DecoderStream(dat, ind);
		int num = dec.readInt();
		if ((num < 0) || (num > (ind.length - 4) / 4)) {
			throw new IOException("Decoder failure!");
		}
		byte out[] = new byte[num];
		for (int i = 0; i < out.length; ++ i) {
			out[i] = (byte) dec.readByte();
		}
		return out;
	}

}
