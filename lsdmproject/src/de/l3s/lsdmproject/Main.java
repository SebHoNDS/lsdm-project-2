package de.l3s.lsdmproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.google.gson.JsonSyntaxException;

import de.l3s.lsdmproject.FilteringTweetByHashtags.CheckCombinationMode;
import de.l3s.lsdmproject.model.Tweet;
import de.l3s.lsdmproject.utils.StopWatch;


public class Main
{

  public static void main(String[] args)
    throws JsonSyntaxException, IOException
  {
    if (args == null || args.length < 1)
    {
      taskException();
    }

    Task task = null;

    try
    {
      task = Task.valueOf(args[0].toUpperCase());
    }
    catch (IllegalArgumentException e)
    {
      taskException();
    }

    if (task == Task.FILTER)
    {
      filterMain(Arrays.copyOfRange(args, 1, args.length));
    }
    else if (task == Task.COUNT)
    {
      countMain(Arrays.copyOfRange(args, 1, args.length));
    }

  }


  private static void taskException()
  {
    throw new IllegalArgumentException("No valid task given.\r\nPossible values: filter, count");
  }


  private static void filterMain(String[] args)
    throws JsonSyntaxException, IOException
  {
    if (args.length < 6)
    {
      throw new IllegalArgumentException(
          "Missing arguments.\r\nExpected: filter {bucketSize[int]} {caseInsensitive[bool]} {countFalsePositives[bool]} {ResultsCombinationMode[EVERY|ANY]} {sourceFilePath[String]} {hashtag[String]} ... {hashtag[String]}");
    }

    final int bucketSize;
    final boolean caseInsensitive;
    final boolean countFalsePositives;
    final CheckCombinationMode mode;
    final String sourceFile;
    final String[] trainingHashtags;

    // Parse Arguments
    try
    {
      bucketSize = Integer.parseInt(args[0]);
      caseInsensitive = Boolean.parseBoolean(args[1]);
      countFalsePositives = Boolean.parseBoolean(args[2]);
      mode = CheckCombinationMode.valueOf(args[3].toUpperCase());
      sourceFile = args[4];
      trainingHashtags = Arrays.copyOfRange(args, 5, args.length);
    }
    catch (Exception e)
    {
      throw new IllegalArgumentException("Filter: Something went wrong while parsing command-line arguments.", e);
    }

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


  private static void countMain(String[] args)
    throws JsonSyntaxException, IOException
  {
    final boolean caseInsensitive;
    final long n;
    final boolean withActualCount;
    final String sourceFile;
    final String[] hashtags;
    final Long[] ks;

    try
    {
      caseInsensitive = Boolean.parseBoolean(args[0]);
      if (args[1].equalsIgnoreCase("everything"))
      {
        n = System.currentTimeMillis();
      }
      else
      {
        n = Long.parseLong(args[1]) * 1000;
      }
      withActualCount = Boolean.parseBoolean(args[2]);
      sourceFile = args[3];

      final ArrayList<Long> ksList = new ArrayList<>();
      final ArrayList<String> hashtagsList = new ArrayList<>();

      for (int i = 4; i < args.length; i++)
      {
        if (args[i].matches("^\\d+$"))
        {
          ksList.add(Long.parseLong(args[i]) * 1000);
        }
        else
        {
          hashtagsList.add(args[i]);
        }
      }

      String[] tempHashtags = new String[hashtagsList.size()];
      hashtagsList.toArray(tempHashtags);
      hashtags = tempHashtags;

      Long[] tempKs = new Long[ksList.size()];
      ksList.toArray(tempKs);
      ks = tempKs;

    }
    catch (Exception e)
    {
      throw new IllegalArgumentException("Count: Something went wrong while parsing command-line arguments.", e);
    }

    System.out.println("[CountingHashTags]");

    System.out.println("");

    System.out.println("----------");
    System.out.println("Parameters");
    System.out.println("----------");
    System.out.println("From file:              " + sourceFile);
    System.out.println("For Hashtags:           " + Arrays.toString(hashtags));
    System.out.println("Case insensitive:       " + caseInsensitive);
    System.out.println("Counting timespans (k): " + Arrays.toString(ks) + " milliseconds");
    System.out.println("Max Timespan (n):       " + n + " milliseconds");
    System.out.println("Compare to actual:      " + withActualCount);
    System.out.println("----------");

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
        CountingHashTags.showFrequency(k, withActualCount, cht, hashtag);
        System.out.println("----------");
        System.out.println("Runtime: " + stopWatches[i].diffString() + " milliseconds");
        System.out.println("");
        System.out.println("");
        i++;
      }

    }
  }


  public enum Task
  {
    FILTER, COUNT
  }

}
