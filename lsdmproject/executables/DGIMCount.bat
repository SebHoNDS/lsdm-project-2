@echo off

java -jar LSDMProject.jar COUNT true everything true "ebola.json" WHO ebola 172800 604800

REM ==DESCRIPTION==
REM COUNT:			Execute a DGIM Count task.
REM true:			Case insensitive counting of hashtags.
REM everything:		Timespan to cover. A duration *IN SECONDS* or "everything".
REM true:			Also counts the actual occurrences of hashtags and prints the absolute/relative errors of DGIM.
REM "path":			Path to the source file. Linewise JSON encoded Tweets.
REM WHO ebola ...	A mixed list of hashtags to count and timespans *IN SECONDS* to print.
REM					I.e. 'WHO ebola 172800 604800' will print results for Hashtags #WHO and #ebola, 2 days and 7 days in the past from the last captured timestamp.
