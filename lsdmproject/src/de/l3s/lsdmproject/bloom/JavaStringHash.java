package de.l3s.lsdmproject.bloom;

public class JavaStringHash
  extends HashFunction
{

  public JavaStringHash(final int n)
  {
    super(n);
  }


  @Override
  public int hash(final String hash)
  {
    // Guarantee positive value between 0 and n-1
    return Math.abs(hash.hashCode() % n);
  }


  @Override
  public String toString()
  {
    return "abs(String.hashCode() mod N)";
  }

}
