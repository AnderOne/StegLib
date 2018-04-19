/**
 * This package includes classes that allow to hide arbitrary data into bitmaps by simple steganography algorithm.
 * <br>
 * The main idea of the algorithm is based on using lower bits of some components in RGBA to store of hidden data.
 * <br>
 * [A][R][G][B] -- pixel presented in RGBA color space.
 * <br>
 * In this scheme each byte D of input data is decomposed into four part, two bits each.
 * <br>
 * Each part written to lower bits of R and/or B bytes.
 * <br>
 * Here we use R and B parts only, because it allows to doesn't much spoil input image.
 */
package steg.lib.img;
