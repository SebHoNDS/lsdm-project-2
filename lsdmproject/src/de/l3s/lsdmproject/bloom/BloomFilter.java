package de.l3s.lsdmproject.bloom;

import java.util.ArrayList;


/**
 * The implementation of a Bloom Filter.
 * 
 * @author Sebastian
 *
 */
public class BloomFilter
{

  private final HashCombo hashCombo;

  private final boolean[] bins;


  public BloomFilter(final int n, final HashCombo hashCombo)
  {
    if (hashCombo.getN() != n)
    {
      throw new IllegalArgumentException("The maximum hash size (" + hashCombo.getN()
          + ") of the given hash combo doesn't match the specified maximum hash size (" + n
          + ") in this Bloom filter.");

    }

    this.hashCombo = hashCombo;
    this.bins = new boolean[n];
  }


  /**
   * Trains this Bloom Filter to accept the given input.
   */
  public void train(final String input)
  {
    final ArrayList<Integer> hashes = hashCombo.hash(input);

    for (final Integer hash : hashes)
    {
      bins[hash] = true;
    }
  }


  /**
   * Returns whether a given input may be matched by the Bloom Filter.
   */
  public boolean check(final String input)
  {
    final ArrayList<Integer> hashes = hashCombo.hash(input);

    for (final Integer hash : hashes)
    {
      if (!bins[hash])
      {
        return false;
      }
    }

    return true;
  }


  public String getPrettyHashFunctions()
  {
    return hashCombo.getPrettyHashFunctions();
  }
}
