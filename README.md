
# TLSDM 2016 Project 2 (by Sebastian Holzki)
## The Sourcecode
All the sources can be found under lsdmproject/src/de/l3s/lsdmproject/ in this repository.
The only required library can be found under lsdmproject/libs/ in this repository. It is automatically linked with the .classpath file.
The executable JAR and run scripts can be found under lsdmproject/executables/ in this repository.

**The project requires at least Java 7 to run!**

## Running the JAR using batch scripts

In the following two sections there are hints on the options available with the compiled JAR file.
The first parameter of the JAR file is the name of the task, either `COUNT` or `FILTER`.

### Counting 1's (DGIM method)

File: DGIMCount.bat

Options:

1. *Case insensitive:* true/false - Whether to count occurrences of hashtags ignoring their case.

2. *Timespan (n):* The timespan to count for. DGIM Buckets that exceed this limit will be discarded. Use some positive number to specifiy the timestamp **in seconds** or use the keyword `everything` to disable this limit.

3. *Count actual occurrences:* true/false - Whether to count and print out the actual number of occurrences of hashtags for comparison. **This may slow down the counting process.**

4. *Source file:* Specify an absolute or relative path to a source file. The format should be JSON encoded Twitter Tweets, one per line.

5.* Hashtags and Timespans:* A space separated list of Hashtags **ànd** timespans **in Seconds**. For Hashtags including spaces, you have to double quote them on windows machines, i.e. `"flu shot"` instead of `flu shot`. You are allowed to specify as much Hashtags as you want to. Also encodes the timespans in the past to count within. These timespans are noted in seconds. I.e. `WHO ebola 172800 604800` counts occurrences of #WHO and #ebola separately within the last two and seven days accordingly. The result will be four distinct counts.


Arguments example 1:
`COUNT true everything true "ebola.json" WHO ebola 172800 604800`
Arguments example 2:
`COUNT true everything true "C:/Users/Max Mustermann/ebola.json" flushot 86400`

### Filtering (Bloom Filters)

File: BloomFilter.bat

Options:

1. *Bucket size (n):* The size of the Array used to store the training results and match the input against.

2. *Case-insensitive:* true/false - Whether to filter occurrences of hashtags ignoring their case.

3. *Count false positives:* true/false - Whether to check the filtered Tweets against the original list of hashtags. The number of false positive filtered Tweets will be printed.

4. *Aggregation mode:* `DISJUNCTION` or `CONJUNCTION` specifies whether filtered Tweets should contain at least one Hashtag of the given list or have to contain all these hashtags.

5. *Source file:* Specify an absolute or relative path to a source file. The format should be JSON encoded Twitter Tweets, one per line.

6. *Training Hashtags:* A space separated list of hashtags to use for training of the Bloom Filter. Hashtags that include spaces have to be double quoted on windows machines.
