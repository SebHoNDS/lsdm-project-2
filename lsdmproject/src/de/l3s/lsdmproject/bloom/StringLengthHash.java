package de.l3s.lsdmproject.bloom;

public class StringLengthHash
  extends HashFunction
{

  public StringLengthHash(final int n)
  {
    super(n);
  }


  @Override
  public int hash(final String hash)
  {
    // Guarantee positive value between 0 and n-1
    return hash.length() % n;
  }


  @Override
  public String toString()
  {
    return "String.length() mod N";
  }

}
