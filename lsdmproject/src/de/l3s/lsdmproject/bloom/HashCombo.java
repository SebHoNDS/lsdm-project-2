package de.l3s.lsdmproject.bloom;

import java.util.ArrayList;


/**
 * Stores one or more hash functions for Strings.
 * 
 * @author Sebastian
 */
public class HashCombo
{

  private final ArrayList<HashFunction> hashFunctions = new ArrayList<>();

  private final int n;


  public HashCombo(final int n, final HashFunction hashFunction)
  {
    this.n = n;
    addHashFunction(hashFunction);
  }


  public int getN()
  {
    return n;
  }


  /**
   * Adds another hash function to this combo.
   */
  public void addHashFunction(final HashFunction hashFunction)
  {
    if (hashFunction.getN() != n)
    {
      throw new IllegalArgumentException("The maximum hash size (" + hashFunction.getN()
          + ") of the given hash function doesn't match the specified maximum hash size (" + n
          + ") in this Hash combo.");
    }

    hashFunctions.add(hashFunction);
  }


  /**
   * Evaluates given input with all hash functions contained in the combo.
   */
  public ArrayList<Integer> hash(final String hash)
  {
    final ArrayList<Integer> hashes = new ArrayList<>(hashFunctions.size());

    for (final HashFunction hashFunction : hashFunctions)
    {
      hashes.add(hashFunction.hash(hash));
    }

    return hashes;
  }


  public String getPrettyHashFunctions()
  {
    String out = hashFunctions.get(0).toString();

    for (int i = 1; i < hashFunctions.size(); i++)
    {
      out += " AND " + hashFunctions.get(i).toString();
    }

    return out;
  }

}
