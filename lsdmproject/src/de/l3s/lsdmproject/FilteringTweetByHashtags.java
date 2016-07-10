package de.l3s.lsdmproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import de.l3s.lsdmproject.bloom.BloomFilter;
import de.l3s.lsdmproject.bloom.HashCombo;
import de.l3s.lsdmproject.bloom.HashFunction;
import de.l3s.lsdmproject.bloom.JavaStringHash;
import de.l3s.lsdmproject.bloom.StringLengthHash;
import de.l3s.lsdmproject.model.Tweet;
import de.l3s.lsdmproject.utils.StopWatch;


public class FilteringTweetByHashtags
{

  final BloomFilter filter;
  final boolean caseInsensitive;
  final boolean countFalsePositives;
  final List<String> trainedHashtags = new ArrayList<>();

  final HashSet<String> falsePositiveHashtags = new HashSet<>();

  long tweetsOverall = 0;
  long tweetsAccepted = 0;
  long tweetsDiscarded = 0;
  long tweetsAcceptedFalsePositive = 0;


  /**
   * Initializes and trains a bloom filter to select Tweets out of a file (stream), that contain certain hashtags.
   */
  public FilteringTweetByHashtags(final String[] trainingHashtags, final boolean caseInsensitive, final int bucketSize,
      final boolean countFalsePositives)
  {
    this.filter = new BloomFilter(bucketSize, getHashCombo(bucketSize));
    this.caseInsensitive = caseInsensitive;
    this.countFalsePositives = countFalsePositives;

    for (final String hashtag : trainingHashtags)
    {
      if (this.caseInsensitive)
      {
        filter.train(hashtag.toLowerCase());
        this.trainedHashtags.add(hashtag.toLowerCase());
      }
      else
      {
        filter.train(hashtag);
        this.trainedHashtags.add(hashtag);
      }
    }

    System.out.println("  ----------");
    System.out.println("  Parameters");
    System.out.println("  ----------");
    System.out.println("  Training Hashtags:     " + Arrays.toString(trainingHashtags));
    System.out.println("  Bucket size (n):       " + bucketSize);
    System.out.println("  Case insensitive:      " + caseInsensitive);
    System.out.println("  Hash functions:        " + this.filter.getPrettyHashFunctions());
    System.out.println("  ----------");
  }


  public static void main(String[] args)
    throws IOException
  {
    final String[] trainingHashtags = { "WHO", "stopebola" };
    final int bucketSize = 100;
    final boolean caseInsensitive = true;
    final boolean countFalsePositives = true;
    final String sourceFile = "E:/tagging-ide/lsdm16/ebola.json";
    final CheckCombinationMode mode = CheckCombinationMode.CONJUNCTION;

    System.out.println("[FilteringTweetByHashtags]");

    System.out.println("");

    System.out.println("Training Bloom Filter...");
    final StopWatch initTimer = new StopWatch("Training Bloom Filter");
    final FilteringTweetByHashtags fht =
      new FilteringTweetByHashtags(trainingHashtags, caseInsensitive, bucketSize, countFalsePositives);
    initTimer.end();
    System.out.println("Done.");

    System.out.println("");

    System.out.println("Analyzing Tweet File...");
    final StopWatch filterTimer = new StopWatch("Analyzing Tweet File");
    final List<Tweet> filteredTweets = fht.analyzeTwitterFileDisjunct(sourceFile, mode);
    fht.printLastStats();
    filterTimer.end();
    System.out.println("Done.");

    System.out.println("");

    System.out.println("Runtimes");
    System.out.println("  " + initTimer.printEnd());
    System.out.println("  " + filterTimer.printEnd());
  }


  /**
   * Creates a combo of hashCode() hashing and length hashing.
   */
  private static HashCombo getHashCombo(final int n)
  {
    final HashFunction defaultHashFunction = new JavaStringHash(n);
    final HashCombo hashCombo = new HashCombo(n, defaultHashFunction);
    hashCombo.addHashFunction(new StringLengthHash(n));

    return hashCombo;
  }


  /**
   * Analyzes a given twitter file by using the trained Bloom Filter.
   */
  public List<Tweet> analyzeTwitterFileDisjunct(final String sourcePath, final CheckCombinationMode mode)
    throws JsonSyntaxException, IOException
  {
    // Check file existence
    final File sourceFile = new File(sourcePath);
    if (!sourceFile.isFile())
    {
      throw new IllegalArgumentException(sourceFile.getAbsolutePath() + " is not a valid file!");
    }

    System.out.println("  ----------");
    System.out.println("  Parameters");
    System.out.println("  ----------");
    System.out.println("  Tweet source File:     " + sourceFile.getAbsolutePath());
    System.out.println("  Count false positives: " + countFalsePositives);
    System.out.println("  Check for Hashtags:    " + mode);
    System.out.println("  ----------");

    // Reset counters
    tweetsOverall = 0;
    tweetsAccepted = 0;
    tweetsDiscarded = 0;
    tweetsAcceptedFalsePositive = 0;

    // Read file linewise
    try (FileReader fr = new FileReader(sourceFile); BufferedReader br = new BufferedReader(fr))
    {
      final ArrayList<Tweet> filteredTweets = new ArrayList<>();

      final Gson gson = new GsonBuilder().create();
      String line;
      while ((line = br.readLine()) != null)
      {
        final Tweet t = gson.fromJson(line, Tweet.class);
        tweetsOverall++;

        /**
         * Test disjunction of hash results.
         */
        if (mode == CheckCombinationMode.DISJUNCTION && checkTweetDisjunct(t))
        {
          filteredTweets.add(t);
          tweetsAccepted++;
        }
        /**
         * Test conjunction of hash results.
         */
        else if (mode == CheckCombinationMode.CONJUNCTION && checkTweetConjunct(t))
        {
          filteredTweets.add(t);
          tweetsAccepted++;
        }
        /**
         * Definitely not acceptable.
         */
        else
        {
          tweetsDiscarded++;
        }

      }

      return filteredTweets;
    }
    catch (FileNotFoundException e)
    {
      throw new IllegalArgumentException(sourceFile.getAbsolutePath() + " is not a valid file!");
    }
  }


  public void printLastStats()
  {
    System.out.println("  Statistics");
    System.out.println("  ----------");
    System.out.println("  Tweets analyzed:       " + tweetsOverall);
    System.out.println("  Tweets discarded:      " + tweetsDiscarded);
    System.out.println("  Tweets accepted");
    System.out.println("          (overall):     " + tweetsAccepted);
    if (countFalsePositives)
    {
      System.out.println("  Tweets accepted");
      System.out.println("   (false positives):    " + tweetsAcceptedFalsePositive);
      System.out.println("  Colliding hashtags:    " + Arrays.toString(falsePositiveHashtags.toArray()));
    }

    System.out.println("  ----------");
  }


  /**
   * Checks whether a given hashtag is matched by the Bloom Filter.<br />
   * 
   * @return False iff the hashtag is definitely not acceptable, false if it is acceptable (including some uncertainty).
   */
  private boolean checkHashtag(final String hashtag)
  {
    if (caseInsensitive)
    {
      return filter.check(hashtag.toLowerCase());
    }
    else
    {
      return filter.check(hashtag);
    }
  }


  /**
   * Checks whether at least one of the tweets' hashtags is matched by the Bloom Filter.
   */
  private boolean checkTweetDisjunct(final Tweet tweet)
  {
    for (final String hashtag : tweet.getHashtags())
    {
      if (checkHashtag(hashtag))
      {
        if (countFalsePositives)
        {
          countFalsePositive(tweet.getId(), hashtag);
        }

        return true;
      }
    }

    return false;
  }


  /**
   * Checks whether all of the tweets' hashtags are matched by the Bloom Filter.
   */
  private boolean checkTweetConjunct(final Tweet tweet)
  {
    for (final String hashtag : tweet.getHashtags())
    {
      if (!checkHashtag(hashtag))
      {
        return false;
      }
    }

    if (tweet.getHashtags().isEmpty())
    {
      return false;
    }
    else
    {
      if (countFalsePositives)
      {
        countFalsePositives(tweet.getId(), tweet.getHashtags());
      }

      return true;
    }
  }


  /**
   * Helper to count false positive accepted tweets and their colliding hashtags for disjunction mode.
   */
  private void countFalsePositive(final long tweetId, final String hashtag)
  {
    if (caseInsensitive)
    {
      // List of filter-hashtags doesn't contain the hashtag (case-insensitive)
      if (!trainedHashtags.contains(hashtag.toLowerCase()))
      {
        tweetsAcceptedFalsePositive++;
        falsePositiveHashtags.add(hashtag);
        // System.out.println(
        // " False positive: Tweet[" + tweetId + "] with hashtag #" + hashtag + " matched (case-insensitive)");
      }
    }
    else
    {
      // List of filter-hashtags doesn't contain the hashtag (case-sensitive)
      if (!trainedHashtags.contains(hashtag))
      {
        tweetsAcceptedFalsePositive++;
        falsePositiveHashtags.add(hashtag);
        // System.out.println(" False positive: Tweet[" + tweetId + "] with hashtag #" + hashtag + " matched");
      }
    }
  }


  /**
   * Helper to count false positive accepted tweets and their colliding hashtags for conjunction mode.
   */
  private void countFalsePositives(final long tweetId, final Collection<String> hashtags)
  {
    if (caseInsensitive)
    {
      boolean falsePositive = false;

      for (String hashtag : hashtags)
      {
        // List of filter-hashtags doesn't contain the hashtag (case-insensitive)
        if (!this.trainedHashtags.contains(hashtag.toLowerCase()))
        {
          falsePositiveHashtags.add(hashtag);
          falsePositive = true;
        }
      }

      if (falsePositive)
      {
        tweetsAcceptedFalsePositive++;
      }
    }
    else
    {
      boolean falsePositive = false;

      for (String hashtag : hashtags)
      {
        // List of filter-hashtags doesn't contain the hashtag (case-insensitive)
        if (!this.trainedHashtags.contains(hashtag))
        {
          falsePositiveHashtags.add(hashtag);
          falsePositive = true;
        }
      }

      if (falsePositive)
      {
        tweetsAcceptedFalsePositive++;
      }
    }
  }


  public enum CheckCombinationMode
  {
    DISJUNCTION, CONJUNCTION
  }

}
