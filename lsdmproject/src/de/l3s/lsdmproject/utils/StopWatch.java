package de.l3s.lsdmproject.utils;

public class StopWatch
{

  final String name;

  final long begin;

  long end = -1;


  public StopWatch(final String name)
  {
    this.name = name;
    begin = System.nanoTime();
  }


  public long end()
  {
    if (end == -1)
    {
      end = System.nanoTime();
    }

    return end;
  }


  private double diffMs()
  {
    return (end() - begin) / 1000000.0;
  }


  public String diffString()
  {
    return String.format("%.4f", diffMs());
  }


  public String printEnd()
  {
    end();
    return "StopWatch[" + name + "] " + diffMs() + " milliseconds";
  }
}
