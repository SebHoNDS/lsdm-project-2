package de.l3s.lsdmproject.dgim;

import de.l3s.lsdmproject.utils.Helper;


/**
 * Represents a DGIM bucket spanning from {@link #beginTime} to {@link #endTime} with a fixed count of (2^{@link #bucketSizePower}).
 * 
 * @author Sebastian
 *
 */
class DGIMBucket
{

  private DGIMBucket nextBucket = null;

  public final int bucketSizePower;

  public final long beginTime;
  public final long endTime;


  /**
   * Creates a bucket of size 1.
   */
  public DGIMBucket(final long time)
  {
    this.bucketSizePower = 0;
    this.beginTime = time;
    this.endTime = time;
  }


  /**
   * Merges two buckets.
   * 
   * @param bucketA
   * @param bucketB
   */
  public DGIMBucket(final DGIMBucket bucketA, final DGIMBucket bucketB)
  {
    if (bucketA.bucketSizePower != bucketB.bucketSizePower)
    {
      // This may happen if called manually for whatever reason
      throw new IllegalArgumentException("Buckets of different sizes cannot be merged.");
    }

    // Earliest begin time
    this.beginTime = Math.min(bucketA.beginTime, bucketB.beginTime);
    // Latest end time
    this.endTime = Math.max(bucketA.endTime, bucketB.endTime);

    // We know that the bucket size is equal in both, bucket A and B.
    bucketSizePower = bucketA.bucketSizePower + 1;
  }


  /**
   * Number of 1's within this bucket. (a power of two)
   */
  public long getBucketCount()
  {
    return Helper.powerOfTwo(bucketSizePower);
  }


  /**
   * Difference of begin and end time of this bucket.
   */
  public long getBucketSpan()
  {
    return endTime - beginTime;
  }


  /**
   * The next bucket in the chain or null, if this bucket is the last one.
   */
  public DGIMBucket getNextBucket()
  {
    return nextBucket;
  }


  /**
   * Detaches the next bucket of this bucket from the chain.
   */
  public void detachNextBucket()
  {
    nextBucket = null;
  }


  /**
   * Prepends this bucket to the front of another bucket. If the action would result in a violation of the 'two buckets per power at
   * maximum' rule, the method handles the issue. The preceding buckets will be merged as suggested in the DGIM algorithm.
   * 
   * @param nextBucketCandidate
   *          The bucket to prepend this bucket to.
   */
  public void prependTo(final DGIMBucket nextBucketCandidate)
  {

    // This is the third bucket with same size, consolidate the whole chain of buckets
    // If there is a next bucket AND a bucket after next AND both of their power-sizes are equal to the power-size of this bucket
    if (nextBucketCandidate != null && nextBucketCandidate.nextBucket != null
        && nextBucketCandidate.bucketSizePower == bucketSizePower
        && nextBucketCandidate.nextBucket.bucketSizePower == bucketSizePower)
    {
      final DGIMBucket restOfChain = nextBucketCandidate.nextBucket.nextBucket;
      final DGIMBucket mergedBucket = new DGIMBucket(nextBucketCandidate, nextBucketCandidate.nextBucket);

      // Prepend to rest of chain and consolidate remaining buckets recursively, if necessary
      mergedBucket.prependTo(restOfChain);

      // Complete the chain by prepending this bucket to the remaining bucket chain
      this.nextBucket = mergedBucket;
    }

    // Otherwise, just prepending this bucket is fine
    else
    {
      this.nextBucket = nextBucketCandidate;
    }

  }


  @Override
  public String toString()
  {
    return "Bucket[2^" + bucketSizePower + "] count=" + getBucketCount() + ", span=" + getBucketSpan();
  }

}
