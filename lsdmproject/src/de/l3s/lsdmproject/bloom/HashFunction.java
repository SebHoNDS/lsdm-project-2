package de.l3s.lsdmproject.bloom;

public abstract class HashFunction
{

  /**
   * The number of different hash results at max.
   */
  final int n;


  public HashFunction(final int n)
  {
    this.n = n;
  }


  /**
   * Returns the number of different hash results at max.
   */
  public int getN()
  {
    return n;
  }


  public abstract int hash(String hash);

}
