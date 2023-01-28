package optifine.xdelta;

public class BitArray {
      int[] implArray;
      int size;
      static final int INT_SIZE = 32;

      public BitArray(int size) {
            int implSize = size / 32 + 1;
            this.implArray = new int[implSize];
      }

      public void set(int pos, boolean value) {
            int implPos = pos / 32;
            int bitMask = 1 << (pos & 31);
            int[] var10000;
            if (value) {
                  var10000 = this.implArray;
                  var10000[implPos] |= bitMask;
            } else {
                  var10000 = this.implArray;
                  var10000[implPos] &= ~bitMask;
            }

      }

      public boolean get(int pos) {
            int implPos = pos / 32;
            int bitMask = 1 << (pos & 31);
            return (this.implArray[implPos] & bitMask) != 0;
      }
}
