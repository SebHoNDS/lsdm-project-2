@echo off

java -jar LSDMProject.jar FILTER 1000 true true DISJUNCTION "ebola.json" WHO stopebola

REM ==DESCRIPTION==
REM FILTER:			Execute a Bloom Filter task.
REM 1000:			Bucket size of the Bloom Filter.
REM true:			Filter hashtags case-insensitive?
REM true:			Count and print false positives?
REM DISJUNCTION:	How to aggregate the hashtag queries for a tweet (Is it sufficient if one hashtag is included (DISJUNCTION) or do all hashtags have to be included (CONJUNCTION)?)
REM "path":			Path to the source file. Linewise JSON encoded Tweets.
REM WHO stopebola:	Space separated list of hastags to use for training of the Bloom Filter.
