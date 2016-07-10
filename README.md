
# TLSDM 2016 Project 2 (by Sebastian Holzki)
## The sourcecode
All the sources can be found under lsdmproject/src/de/l3s/lsdmproject/ in this repository.
The only required library can be found under lsdmproject/libs/ in this repository. It is automatically linked with the .classpath file.
The executable JAR and run scripts can be found under lsdmproject/executables/ in this repository.

**The project requires at least Java 7 to run!**

## Running the JAr using batch scripts

In the following two sections there are hints on the options available with the compiled JAR file.
The first parameter of the JAR file is the name of the task, either `COUNT` or `FILTER`.

### Counting 1's (DGIM method)

File: DGIMCount.bat
1. Case insensitive: true/false
2. Timespan (n): The timespan to count for. DGIM Buckets that exceed this limit will be discarded. Use some positive number to specifiy the timestamp **in seconds** or use the keyword `everything` to disable this limit.
3. Count actual occurrences: Whether to count and print out the actual number of occurrences of hashtags for comparison. **This may slow down the counting process.**
4. Source file: Specify an absolute or relatove path to a source file. The format should be JSON encoded Twitter Tweets, one per line.
5. The Hashtags: A space separated list of Hashtags to count wothin the Tweets from the source file. For Hashtags including spaces, you have to double quote them on windows machines, i.e. `"flu shot"` instead of `flu shot`. You are allowed to specify as much Hashtags as you want to.

### Filtering (Bloom Filters)
