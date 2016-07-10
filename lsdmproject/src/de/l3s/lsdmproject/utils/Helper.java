package de.l3s.lsdmproject.utils;

public class Helper
{

  public static long powerOfTwo(final int power)
  {
    if (power < 0)
    {
      throw new IllegalArgumentException("Negative powers are not supported.");
    }
    else if (power > 62)
    {
      throw new IllegalArgumentException("Powers greater than 62 cannot be represented using long (64 bit).");
    }

    return (long) 1 << power;
  }
}
