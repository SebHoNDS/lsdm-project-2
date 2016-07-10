package de.l3s.lsdmproject.baseline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import de.l3s.lsdmproject.model.Tweet;


public class NaiiveCount
{

  private String name;

  private long timer = 0;

  private final long maximumStorageLength;

  private final HashMap<Tweet, Boolean> tweets = new HashMap<>();


  public NaiiveCount(final long n, final String name)
  {
    this.name = name;
    this.maximumStorageLength = n;
  }


  public void insert(final Tweet tweet, final boolean value)
  {
    final long timestamp = tweet.getTimestampMs();
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
      tweets.put(tweet, value);
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

    // Sort the tweets
    ArrayList<Tweet> sortedTweets = new ArrayList<>(tweets.keySet());
    Collections.sort(sortedTweets, new TweetDescTimestampComparator());

    long count = 0;
    for (Tweet tweet : sortedTweets)
    {
      // Bucket is completely within the window of k.
      if ((timer - tweet.getTimestampMs()) <= k && tweets.get(tweet).booleanValue())
      {
        count++;
      }
      // k is exceeded
      else
      {
        break;
      }
    }

    return count;
  }


  public String getName()
  {
    return name;
  }


  private class TweetDescTimestampComparator
    implements Comparator<Tweet>
  {

    @Override
    public int compare(Tweet o1, Tweet o2)
    {
      final long diff = o2.getTimestampMs() - o1.getTimestampMs();
      if (diff < 0)
      {
        return -1;
      }
      else if (diff > 0)
      {
        return 1;
      }
      else
      {
        return 0;
      }

    }

  }

}
