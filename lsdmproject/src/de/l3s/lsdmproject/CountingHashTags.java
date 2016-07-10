package de.l3s.lsdmproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import de.l3s.lsdmproject.baseline.NaiiveCount;
import de.l3s.lsdmproject.dgim.DGIMCount;
import de.l3s.lsdmproject.model.Tweet;
import de.l3s.lsdmproject.utils.StopWatch;


public class CountingHashTags
{

  final boolean caseInsensitive;
  final String[] hashtags;
  final long n;

  final DGIMCount[] dgimCounters;
  final NaiiveCount[] naiiveCounters;
  final HashMap<String, DGIMCount> mappedDgimCounters = new HashMap<>();
  final HashMap<String, NaiiveCount> mappedNaiiveCounters = new HashMap<>();


  public CountingHashTags(final Set<String> hashtags, final boolean caseInsensitive, final long n)
  {
    this.caseInsensitive = caseInsensitive;
    this.hashtags = hashtags.toArray(new String[hashtags.size()]);
    this.n = n;

    // Instantiate counters
    dgimCounters = new DGIMCount[this.hashtags.length];
    naiiveCounters = new NaiiveCount[this.hashtags.length];
    int i = 0;
    for (String hashtag : this.hashtags)
    {
      dgimCounters[i] = new DGIMCount(this.n, hashtag);
      mappedDgimCounters.put(hashtag, dgimCounters[i]);
      naiiveCounters[i] = new NaiiveCount(n, hashtag);
      mappedNaiiveCounters.put(hashtag, naiiveCounters[i]);
      i++;
    }
  }


  public static void main(String[] args)
    throws JsonSyntaxException, IOException
  {
    final String sourceFile = "E:/tagging-ide/lsdm16/ebola.json";
    final String[] hashtags = { "ebola", "WHO", "SierraLeone" };
    final boolean caseInsensitive = true;
    final long n = System.currentTimeMillis(); // Capture everything
    final long[] ks = { 2 * 86400 * 1000, 7 * 86400 * 1000, 365 * 86400 * 1000 };
    final boolean withActualCount = true;

    System.out.println("[CountingHashTags]");
    System.out.println("------------------");
    System.out.println("From file:              " + sourceFile);
    System.out.println("For Hashtags:           " + Arrays.toString(hashtags));
    System.out.println("Case insensitive:       " + Boolean.toString(caseInsensitive));
    System.out.println("Counting timespans (k): " + Arrays.toString(ks) + " milliseconds");
    System.out.println("Max Timespan (n):       " + n + " milliseconds");
    System.out.println("Compare to actual:      " + Boolean.toString(withActualCount));
    System.out.println("------------------");

    System.out.println("");

    final CountingHashTags cht = new CountingHashTags(new HashSet<String>(Arrays.asList(hashtags)), caseInsensitive, n);

    System.out.println("Processing source file...");
    final StopWatch readInsertTimer = new StopWatch("Processing source file");
    final long tweets = cht.analyzeTwitterFile(sourceFile, withActualCount);
    System.out.println(tweets + " Tweets parsed");
    System.out.println(readInsertTimer.printEnd());
    System.out.println("");
    System.out.println("");

    final StopWatch[] stopWatches = new StopWatch[ks.length * hashtags.length];
    int i = 0;
    for (long k : ks)
    {
      for (String hashtag : hashtags)
      {
        stopWatches[i] = new StopWatch("Counting of " + Arrays.toString(hashtags) + " at " + k + " milliseconds");
        showFrequency(k, withActualCount, cht, hashtag);
        System.out.println("-----------");
        System.out.println("Runtime: " + stopWatches[i].diffString() + " milliseconds");
        System.out.println("");
        System.out.println("");
        i++;
      }

    }
  }


  public static void showFrequency(final long k, final boolean withActualCount, final CountingHashTags cht,
      final String counterName)
  {
    final DGIMCount dgim = cht.getCounterForName(counterName);
    final long approx = dgim.count(k);

    if (withActualCount)
    {
      final NaiiveCount naiive = cht.getActualCounterForName(counterName, k);
      final long actual = naiive.count(k);
      final double err = (approx - actual) / (double) actual * 100.0;

      System.out.println("Count #" + counterName + " @ T-" + Long.toString(k) + " milliseconds");
      System.out.println("-----------");
      System.out.println("DGIM:       " + approx);
      System.out.println("Actual:     " + actual);
      System.out.println("Abs. Error: " + (approx - actual));
      System.out.println("Rel. Error: " + String.format("%.3f", err) + "%");
    }
    else
    {
      System.out.println("Count #" + counterName + " @ T-" + Long.toString(k) + " milliseconds");
      System.out.println("-----------");
      System.out.println(approx + " times");
    }
  }


  public DGIMCount getCounterForName(final String name)
  {
    if (!mappedDgimCounters.containsKey(name))
    {
      throw new NoSuchElementException("No counter for '" + name + "'.");
    }

    return mappedDgimCounters.get(name);
  }


  public NaiiveCount getActualCounterForName(final String name, final long k)
  {
    if (!mappedNaiiveCounters.containsKey(name))
    {
      throw new NoSuchElementException("No counter for '" + name + "'.");
    }

    return mappedNaiiveCounters.get(name);
  }


  private long analyzeTwitterFileWithoutActualCount(final String sourcePath)
    throws JsonSyntaxException, IOException
  {
    final File sourceFile = new File(sourcePath);
    if (!sourceFile.isFile())
    {
      throw new IllegalArgumentException(sourceFile.getAbsolutePath() + " is not a valid file!");
    }

    try (FileReader fr = new FileReader(sourceFile); BufferedReader br = new BufferedReader(fr))
    {
      final Gson gson = new GsonBuilder().create();
      long tweetcount = 0;
      long invalidCount = 0;
      String line;
      while ((line = br.readLine()) != null)
      {
        tweetcount++;
        final Tweet t = gson.fromJson(line, Tweet.class);

        if (t.getTimestampMs() == -1)
        {
          System.out.println("Warning: Tweet with null timestamp ignored");
          invalidCount++;
          continue;
        }

        for (int h = 0; h < hashtags.length; h++)
        {
          final boolean contained = t.isHashtagContained(hashtags[h], caseInsensitive);
          dgimCounters[h].insert(t.getTimestampMs(), contained);
        }
      }

      return tweetcount;
    }
    catch (FileNotFoundException e)
    {
      throw new IllegalArgumentException(sourceFile.getAbsolutePath() + " is not a valid file!");
    }
  }


  public long analyzeTwitterFile(final String sourcePath, final boolean withActualCount)
    throws JsonSyntaxException, IOException
  {
    if (withActualCount)
    {
      return analyzeTwitterFileWithActualCount(sourcePath);
    }
    else
    {
      return analyzeTwitterFileWithoutActualCount(sourcePath);
    }
  }


  private long analyzeTwitterFileWithActualCount(final String sourcePath)
    throws JsonSyntaxException, IOException
  {
    final File sourceFile = new File(sourcePath);
    if (!sourceFile.isFile())
    {
      throw new IllegalArgumentException(sourceFile.getAbsolutePath() + " is not a valid file!");
    }

    try (FileReader fr = new FileReader(sourceFile); BufferedReader br = new BufferedReader(fr))
    {
      final Gson gson = new GsonBuilder().create();
      long tweetcount = 0;
      String line;
      while ((line = br.readLine()) != null)
      {
        tweetcount++;
        final Tweet t = gson.fromJson(line, Tweet.class);

        if (t.getTimestampMs() == -1)
        {
          System.out.println("Warning: Tweet with null timestamp ignored");
          continue;
        }

        for (int h = 0; h < hashtags.length; h++)
        {
          final boolean contained = t.isHashtagContained(hashtags[h], caseInsensitive);
          dgimCounters[h].insert(t.getTimestampMs(), contained);
          naiiveCounters[h].insert(t, contained);
        }
      }

      return tweetcount;
    }
    catch (FileNotFoundException e)
    {
      throw new IllegalArgumentException(sourceFile.getAbsolutePath() + " is not a valid file!");
    }
  }

}
