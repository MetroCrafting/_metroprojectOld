package net.minecraft.src;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import org.apache.commons.lang3.StringEscapeUtils;

public class NbtTagValue {
      private String[] parents = null;
      private String name = null;
      private boolean negative = false;
      private int type = 0;
      private String value = null;
      private int valueFormat = 0;
      private static final int TYPE_TEXT = 0;
      private static final int TYPE_PATTERN = 1;
      private static final int TYPE_IPATTERN = 2;
      private static final int TYPE_REGEX = 3;
      private static final int TYPE_IREGEX = 4;
      private static final String PREFIX_PATTERN = "pattern:";
      private static final String PREFIX_IPATTERN = "ipattern:";
      private static final String PREFIX_REGEX = "regex:";
      private static final String PREFIX_IREGEX = "iregex:";
      private static final int FORMAT_DEFAULT = 0;
      private static final int FORMAT_HEX_COLOR = 1;
      private static final String PREFIX_HEX_COLOR = "#";
      private static final Pattern PATTERN_HEX_COLOR = Pattern.compile("^#[0-9a-f]{6}+$");

      public NbtTagValue(String tag, String value) {
            String[] names = Config.tokenize(tag, ".");
            this.parents = (String[])Arrays.copyOfRange(names, 0, names.length - 1);
            this.name = names[names.length - 1];
            if (value.startsWith("!")) {
                  this.negative = true;
                  value = value.substring(1);
            }

            if (value.startsWith("pattern:")) {
                  this.type = 1;
                  value = value.substring("pattern:".length());
            } else if (value.startsWith("ipattern:")) {
                  this.type = 2;
                  value = value.substring("ipattern:".length()).toLowerCase();
            } else if (value.startsWith("regex:")) {
                  this.type = 3;
                  value = value.substring("regex:".length());
            } else if (value.startsWith("iregex:")) {
                  this.type = 4;
                  value = value.substring("iregex:".length()).toLowerCase();
            } else {
                  this.type = 0;
            }

            value = StringEscapeUtils.unescapeJava(value);
            if (this.type == 0 && PATTERN_HEX_COLOR.matcher(value).matches()) {
                  this.valueFormat = 1;
            }

            this.value = value;
      }

      public boolean matches(NBTTagCompound nbt) {
            if (this.negative) {
                  return !this.matchesCompound(nbt);
            } else {
                  return this.matchesCompound(nbt);
            }
      }

      public boolean matchesCompound(NBTTagCompound nbt) {
            if (nbt == null) {
                  return false;
            } else {
                  NBTBase tagBase = nbt;

                  for(int i = 0; i < this.parents.length; ++i) {
                        String tag = this.parents[i];
                        tagBase = getChildTag((NBTBase)tagBase, tag);
                        if (tagBase == null) {
                              return false;
                        }
                  }

                  if (this.name.equals("*")) {
                        return this.matchesAnyChild((NBTBase)tagBase);
                  } else {
                        NBTBase tagBase1 = getChildTag((NBTBase)tagBase, this.name);
                        if (tagBase == null) {
                              return false;
                        } else if (this.matchesBase(tagBase)) {
                              return true;
                        } else {
                              return false;
                        }
                  }
            }
      }

      private boolean matchesAnyChild(NBTBase tagBase) {
            if (tagBase instanceof NBTTagCompound) {
                  NBTTagCompound tagCompound = (NBTTagCompound)tagBase;
                  Set nbtKeySet = tagCompound.func_150296_c();
                  Iterator it = nbtKeySet.iterator();

                  while(it.hasNext()) {
                        String key = (String)it.next();
                        NBTBase nbtBase = tagCompound.getTag(key);
                        if (this.matchesBase(nbtBase)) {
                              return true;
                        }
                  }
            }

            if (tagBase instanceof NBTTagList) {
                  NBTTagList tagList = (NBTTagList)tagBase;
                  int count = tagList.tagCount();

                  for(int i = 0; i < count; ++i) {
                        NBTBase nbtBase = tagList.getCompoundTagAt(i);
                        if (this.matchesBase(nbtBase)) {
                              return true;
                        }
                  }
            }

            return false;
      }

      private static NBTBase getChildTag(NBTBase tagBase, String tag) {
            if (tagBase instanceof NBTTagCompound) {
                  NBTTagCompound tagCompound = (NBTTagCompound)tagBase;
                  return tagCompound.getTag(tag);
            } else if (tagBase instanceof NBTTagList) {
                  NBTTagList tagList = (NBTTagList)tagBase;
                  if (tag.equals("count")) {
                        return new NBTTagInt(tagList.tagCount());
                  } else {
                        int index = Config.parseInt(tag, -1);
                        return index < 0 ? null : tagList.getCompoundTagAt(index);
                  }
            } else {
                  return null;
            }
      }

      public boolean matchesBase(NBTBase nbtBase) {
            if (nbtBase == null) {
                  return false;
            } else {
                  String nbtValue = getNbtString(nbtBase, this.valueFormat);
                  return this.matchesValue(nbtValue);
            }
      }

      public boolean matchesValue(String nbtValue) {
            if (nbtValue == null) {
                  return false;
            } else {
                  switch(this.type) {
                  case 0:
                        return nbtValue.equals(this.value);
                  case 1:
                        return this.matchesPattern(nbtValue, this.value);
                  case 2:
                        return this.matchesPattern(nbtValue.toLowerCase(), this.value);
                  case 3:
                        return this.matchesRegex(nbtValue, this.value);
                  case 4:
                        return this.matchesRegex(nbtValue.toLowerCase(), this.value);
                  default:
                        throw new IllegalArgumentException("Unknown NbtTagValue type: " + this.type);
                  }
            }
      }

      private boolean matchesPattern(String str, String pattern) {
            return StrUtils.equalsMask(str, pattern, '*', '?');
      }

      private boolean matchesRegex(String str, String regex) {
            return str.matches(regex);
      }

      private static String getNbtString(NBTBase nbtBase, int format) {
            if (nbtBase == null) {
                  return null;
            } else if (nbtBase instanceof NBTTagString) {
                  NBTTagString nbtString = (NBTTagString)nbtBase;
                  return nbtString.func_150285_a_();
            } else if (nbtBase instanceof NBTTagInt) {
                  NBTTagInt i = (NBTTagInt)nbtBase;
                  return format == 1 ? "#" + StrUtils.fillLeft(Integer.toHexString(i.func_150287_d()), 6, '0') : Integer.toString(i.func_150287_d());
            } else if (nbtBase instanceof NBTTagByte) {
                  NBTTagByte b = (NBTTagByte)nbtBase;
                  return Byte.toString(b.func_150290_f());
            } else if (nbtBase instanceof NBTTagShort) {
                  NBTTagShort s = (NBTTagShort)nbtBase;
                  return Short.toString(s.func_150289_e());
            } else if (nbtBase instanceof NBTTagLong) {
                  NBTTagLong l = (NBTTagLong)nbtBase;
                  return Long.toString(l.func_150291_c());
            } else if (nbtBase instanceof NBTTagFloat) {
                  NBTTagFloat f = (NBTTagFloat)nbtBase;
                  return Float.toString(f.func_150288_h());
            } else if (nbtBase instanceof NBTTagDouble) {
                  NBTTagDouble d = (NBTTagDouble)nbtBase;
                  return Double.toString(d.func_150286_g());
            } else {
                  return nbtBase.toString();
            }
      }

      public String toString() {
            StringBuffer sb = new StringBuffer();

            for(int i = 0; i < this.parents.length; ++i) {
                  String parent = this.parents[i];
                  if (i > 0) {
                        sb.append(".");
                  }

                  sb.append(parent);
            }

            if (sb.length() > 0) {
                  sb.append(".");
            }

            sb.append(this.name);
            sb.append(" = ");
            sb.append(this.value);
            return sb.toString();
      }
}
