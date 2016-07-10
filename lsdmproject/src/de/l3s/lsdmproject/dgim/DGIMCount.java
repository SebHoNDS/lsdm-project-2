package de.l3s.lsdmproject.dgim;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Represents a DGIM counter. It can be used to approximate the count of items seen in a stream.<br />
 * 
 * @author Sebastian
 *
 */
public class DGIMCount
  implements Iterable<DGIMBucket>
{

  private String name;

  private long timer = 0;

  private final long maximumStorageLength;

  private DGIMBucket firstBucket = null;


  /**
   * Instantiates a DGIM counter. The maximum length of stored stream data is limited by the long data-type.
   * 
   * @param n
   *          Maximum length of stored stream data.
   */
  public DGIMCount(final long n, final String name)
  {
    if (n < 1)
    {
      throw new IllegalArgumentException("Maximum size (n) must be greater than or equal to 1.");
    }
    else if (n > Long.MAX_VALUE)
    {
      throw new IllegalArgumentException("Maximum size (n) is limited to maximum positive value of long (2^63-1).");
    }

    this.name = name;

    maximumStorageLength = n;
  }


  /**
   * Inserts a new value in front of the stream. True for 1, false for 0.
   */
  public void insert(final long timestamp, final boolean value)
  {
    // Can't handle tweets without timestamp
    if (timestamp == -1)
    {
      return;
    }

    if (timestamp > timer)
    {
      this.timer = timestamp;
    }

    // Count only ones (boolean true)
    if (value)
    {
      // Create bucket of size 1
      final DGIMBucket newBucket = new DGIMBucket(timer);
      // Update and consolidate chain of buckets
      newBucket.prependTo(firstBucket);
      // Update entry-point
      firstBucket = newBucket;
    }

    discardOldBucket();
  }


  /**
   * Checks if there is a bucket that exceeds the maximum storage length and removes the bucket, if one.
   */
  public void discardOldBucket()
  {
    for (DGIMBucket bucket : this)
    {
      // If there is a next bucket and it is (partially) outside of the maximum count n
      if (bucket.getNextBucket() != null && bucket.getNextBucket().endTime < (timer - maximumStorageLength))
      {
        System.out.println("Detached a bucket: " + bucket);
        bucket.detachNextBucket();
        break;
      }
    }

  }


  /**
   * Count 1's within the latest k elements.<br />
   * The result is approximated using DGIM method.
   * 
   * @param k
   *          Number of elements to consider.
   * @return The count or 0 for negative k.
   */
  public long count(final long k)
  {
    if (k < 1)
    {
      return 0;
    }
    else if (k > maximumStorageLength)
    {
      throw new IllegalArgumentException(
          "Value of k must be less than or equal to maximum stream length n (" + maximumStorageLength + ").");
    }

    long count = 0;
    for (DGIMBucket b : this)
    {
      // Bucket is completely within the window of k.
      if ((timer - b.beginTime) <= k)
      {
        count += b.getBucketCount();
      }
      // Bucket partially overlaps the window of k.
      else if ((timer - b.endTime) <= k)
      {
        // Add just the affected proportion of the count, assuming uniform distribution
        final double factor = ((double) (k - timer + b.endTime)) / ((double) b.getBucketSpan());
        count += Math.round(factor * b.getBucketCount());

        // All following buckets don't overlap with k anymore
        break;
      }
    }

    return count;
  }


  @Override
  public Iterator<DGIMBucket> iterator()
  {
    return new BucketIterator(firstBucket);
  }


  private static class BucketIterator
    implements Iterator<DGIMBucket>
  {

    private DGIMBucket cursor;


    public BucketIterator(final DGIMBucket firstBucket)
    {
      this.cursor = firstBucket;
    }


    @Override
    public boolean hasNext()
    {
      return cursor != null;
    }


    @Override
    public DGIMBucket next()
    {
      if (hasNext())
      {
        final DGIMBucket current = cursor;
        cursor = current.getNextBucket();

        return current;
      }
      else
      {
        throw new NoSuchElementException();
      }
    }


    @Override
    public void remove()
    {
      throw new UnsupportedOperationException();
    }

  }


  public String getName()
  {
    return name;
  }


  @Override
  public String toString()
  {
    final StringBuffer buf = new StringBuffer("CountingOnes(" + name + "): Timestamp[" + timer + "]\r\n");

    for (DGIMBucket b : this)
    {
      buf.append(b.toString());
      buf.append("\r\n");
    }

    return buf.toString();
  }

}
