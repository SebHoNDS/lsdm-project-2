package de.l3s.lsdmproject.model;

import java.util.HashSet;
import java.util.List;


public class Tweet
{

  private long id;

  private String timestamp_ms;

  private TweetEntities entities;


  /**
   * Returns the tweet's timestamp in milliseconds. In case that the timestamp was null, -1 is returned.
   */
  public long getTimestampMs()
  {
    if (timestamp_ms == null)
    {
      return -1;
    }
    else
    {
      return Long.parseLong(timestamp_ms);
    }
  }


  /**
   * Returns the tweets identifier.
   */
  public long getId()
  {
    return id;
  }


  /**
   * Returns a set of hashtags that were mentioned in the tweet.
   */
  public HashSet<String> getHashtags()
  {
    final HashSet<String> hashtags = new HashSet<>();

    if (entities == null)
    {
      return hashtags;
    }

    final List<TweetHashMention> hashMentions = entities.getHashtags();

    for (TweetHashMention hashMention : hashMentions)
    {
      hashtags.add(hashMention.getHashtag());
    }

    return hashtags;
  }


  /**
   * Returns whether the given hashtag was mentioned in this tweet or not. Optionally you can define the predicate 'contained' as
   * case-sensitive or -insensitive.
   */
  public boolean isHashtagContained(final String hashtag, final boolean caseInsensitive)
  {
    final List<TweetHashMention> hashMentions = entities.getHashtags();

    if (caseInsensitive)
    {
      for (TweetHashMention hashMention : hashMentions)
      {
        final String tweetHashtag = hashMention.getHashtag();
        if (tweetHashtag.equalsIgnoreCase(hashtag))
        {
          return true;
        }
      }
    }
    else
    {
      for (TweetHashMention hashMention : hashMentions)
      {
        if (hashMention.getHashtag().equals(hashtag))
        {
          return true;
        }
      }
    }

    return false;
  }

}
