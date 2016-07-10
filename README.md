
# LSDM 2016 Project 2 (by Sebastian Holzki)
## The Sourcecode
All the sources can be found under [lsdmproject/src/de/l3s/lsdmproject/](https://github.com/SebHoNDS/lsdm-project-2/tree/master/lsdmproject/src/de/l3s/lsdmproject) in this repository.
The only required library can be found under [lsdmproject/libs/](https://github.com/SebHoNDS/lsdm-project-2/tree/master/lsdmproject/libs) in this repository. It is automatically linked with the .classpath file.
The executable JAR and run scripts can be found under [lsdmproject/executables/](https://github.com/SebHoNDS/lsdm-project-2/tree/master/lsdmproject/executables) in this repository.

**The project requires at least Java 7 to run!**

## Running the JAR using batch scripts

In the following two sections there are hints on the options available with the compiled JAR file.
The first parameter of the JAR file is the name of the task, either `COUNT` or `FILTER`.

There are several task-specific options after the task command. These are explained in the sections below.

The general pattern of a command-line call is the following:
`java -jar LSDMProject.jar {TASKNAME} {OPTIONS}`

## Counting 1's (DGIM method) - task name: `COUNT`

See File: `DGIMCount.bat`

#### Options

1. *Case insensitive:* true/false - Whether to count occurrences of hashtags ignoring their case.

2. *Timespan (n):* The timespan to count for. DGIM Buckets that exceed this limit will be discarded. Use some positive number to specifiy the timestamp **in seconds** or use the keyword `everything` to disable this limit.

3. *Count actual occurrences:* true/false - Whether to count and print out the actual number of occurrences of hashtags for comparison. **This may slow down the counting process.**

4. *Source file:* Specify an absolute or relative path to a source file. The format should be JSON encoded Twitter Tweets, one per line.

5. *Hashtags and Timespans:* A space separated list of Hashtags **and** timespans **in seconds**. For Hashtags including spaces, you have to double quote them on windows machines, i.e. `"flu shot"` instead of `flu shot`. You are allowed to specify as much Hashtags as you want to. Also encodes the timespans in the past to count within (Same for timespans). These timespans are denoted in seconds. I.e. `WHO ebola 172800 604800` counts occurrences of #WHO and #ebola separately within the last two and seven days accordingly. The result will be four distinct counts. (All combinations of hashtags and time periods)

#### Example arguments

Arguments example 1:
`COUNT true everything true "ebola.json" WHO ebola 172800 604800`

Counts `#WHO` and `#ebola` (without considering case) for the last `two` and `seven days` from the file `ebola.json`. The maxmimum timespan is unlimited. Furthermore the actual counts and absolute/relative errors are printed, for evaluating the algorithm.

Arguments example 2:
`COUNT true 86400 true "C:/Users/Max Mustermann/ebola.json" flushot 86400`

Counts `#flushot` (without considering case) for the `last day` from the file `C:/Users/Max Mustermann/ebola.json`. The maxmimum timespan is `one day`. Furthermore the actual counts and absolute/relative errors are printed, for evaluating the algorithm.

## Filtering (Bloom Filters) - task name `FILTER`

See File: `BloomFilter.bat`

A Bloom Filter requires a set of hash functions to run. There are two hash-functions included at the moment. The functions are pluggable and can easily be extended or exchanged.

- abs(String.hashCode()) mod N
- String.length() mod N

#### Options

1. *Bucket size (n):* The size of the Array used to store the training results and match the input against.

2. *Case-insensitive:* true/false - Whether to filter occurrences of hashtags ignoring their case.

3. *Count false positives:* true/false - Whether to check the filtered Tweets against the original list of hashtags. The number of false positive Results will be printed. **This option may slow down the computation!**

4. *Aggregation mode:* `DISJUNCTION` or `CONJUNCTION` specifies whether filtered Tweets should contain at least one Hashtag matched by the filter (Disjunction) or if all of the hashtags mentioned in a Tweet have to be matched by the filter (Conjunction).

5. *Source file:* Specify an absolute or relative path to a source file. The format should be JSON encoded Twitter Tweets, one per line.

6. *Training Hashtags:* A space separated list of hashtags to use for training of the Bloom Filter. Hashtags that include spaces have to be double quoted on windows machines.

#### Example arguments

Arguments example 1:
`FILTER 1000 true true DISJUNCTION "E:/lsdm16/ebola.json" WHO stopebola`

Trains a Bloom Filter with Hashtags `#WHO` and `#stopebola` (without considering case) and evaluates Tweets from the file `E:/lsdm16/ebola.json`. Bucket size is 1000. The Tweets are returned iff either #WHO or #stopebola are mentioned in them. For evaluating the performance, also the number of false negatives (in terms of returned Tweets) is printed and a list of Hashtags, that were responsible for false negative results by hash collsions, is provided.

Arguments example 2:
`FILTER 100000 false true CONJUNCTION ebola.json government ebola SierraLeone`

Trains a Bloom Filter with Hashtags `#government`, `#ebola` and `#SierraLeone` (**considering case**) and evaluates Tweets from the file `ebola.json`. Bucket size is 100000. The Tweets are returned iff all hashtags of a tweet are contained in the training list. For evaluating the performance, also the number of false negatives (in terms of returned Tweets) is printed and a list of Hashtags, that were responsible for false negative results by hash collsions, is provided.
