#!/usr/bin/perl

use HTTP::Date;
use POSIX qw(strftime);
use LWP::Simple;
use Statistics::Descriptive;
use strict;

my $dateToday = strftime "%Y-%m-%d", localtime;

my $logFile = $ARGV[0]; 

chomp $logFile;


my %sidTime;
my %sidResCount;
my %sidTimeStamp;
my %sidFilters;
my %sidIndex;
my %sidQuery;
my $sidCount;
my $queryCount;
my $avgQueries;
my %timeBucketCount;
my %countBucketCount;
my %sidQueryCount;
my @sids;
my @slowsids;

#[2009 Aug 06 Thu 16:59:59.451] 0.911 sec [exp(field3:4000,field2:2000,field1:1000)/1/cmp (0,120)/2000 36228] [champu] [lucenseTest] [/127.0.0.1:50570] [0] [my comment] [sf3:(telecom engineer) OR sf2:(telecom engineer) OR sf1:(telecom engineer)]
#my $w = qq/^\\[([^\\]]+)\\] (.+) ... [^\\[]*\\[([^\/]+)\/([^\/]+)\/([^ ]+) ([^ ]+) \\(([^,]+),[^ ]+ \\[([^\\]]+)\\] \\[([^\\]]+)\\] \\[([^\\]]+)\\] (.*)/; 
my $w = qq/^\\[([^\\]]+)\\] ([^\s]+) ... [^\\[]*\\[[^\/]+\/[^\/]+\/[^ ]+ [^\/]+\/[^ ]+ ([^\\]]+)\\] \\[[^\\]]+\\] \\[[^\\]]+\\] \\[[^\\]]+\\] \\[[^\\]]+\\] \\[([^\\]]*)\\]/; 

my  $stamp;
my  $time;
my  $matchmode;
my  $filters;
my  $sortmode;
my  $rescount;
my  $offset;
my  $index;
my  $app;
my  $sid;
my  $query;
my  $secs = 3600;
print "analysing $logFile...\n";

open FILE, "<$logFile";
$sid=0;
while ( <FILE> ) {
    chomp;
    if($_ eq "") {next;}
    m/$w/;
    #print $_;
    
    $stamp       = $1;
    $time        = $2;
    $rescount    = $3;
    $sid++;
   
#    print "$1, $2, $3, $4\n";
    $sidTime{$sid}+=$time;
    $sidResCount{$sid}+=$rescount;
    if( $sidTimeStamp{$sid} eq "") {$sidTimeStamp{$sid}=$stamp;}
    $queryCount++;
    #print "$sid: ".$sidTime{$sid}.",".$sidIndex{$sid}."\n";
}
close FILE;

$sidCount = keys(%sidTime);
@sids = keys(%sidTime);
my $slowcount=0;

my @interval = (0.25,0.5,1.0,1.5,2.0,3.0,5.0,10.0,20.0);
my @countinterval = (1, 6, 11, 21, 41, 81, 201, 401, 801, 2001, 4001, 50001, 1000001, 5000001, 10000001, 20000001);
my $key;
my $flag;
my $slot;
my $timeStampHourly;
my %totalTime;
my %totalReqs;
my %totalCount;

#open MYFILE, ">log.csv";
#open SLOWFILE, ">slowlog.csv";
foreach(keys(%sidTime)) {
    $key = $_;
    $flag = 0;
    foreach(@interval) {
        $slot = $_;
        if($sidTime{$key} < $slot){
            $timeBucketCount{$slot}++;
            $flag = 1;
            $timeStampHourly = $sidTimeStamp{$key};
            $timeStampHourly =~ s/([^:]+).*/$1/i;
            #$timeStampHourly =~ s/([^:]+:[^:]+).*/$1/i;
            $totalTime{$timeStampHourly} += $sidTime{$key};
            $totalReqs{$timeStampHourly}++;
            if($slot==20) { 
                $slowcount++;
            }
            last;
        }
    }
    if(!$flag) {
        $timeBucketCount{9999}++;
        $slowcount++;
    }
    $flag = 0;
    foreach(@countinterval) {
        $slot = $_;
        if($sidResCount{$key} < $slot) {
            $countBucketCount{$slot}++;
            $flag = 1;
            $timeStampHourly = $sidTimeStamp{$key};
            $timeStampHourly =~ s/([^:]+).*/$1/i;
            $totalCount{$timeStampHourly} += $sidResCount{$key};
            last;
        }
    }
    if(!$flag) {
        $countBucketCount{9999}++;
    }
}
#close SLOWFILE;
#close MYFILE;

my @keys = sort { $a cmp $b } keys %totalTime;





my $stat = Statistics::Descriptive::Full->new();
$stat->add_data(values(%sidTime));

print "\n_________________ Query Time Analysis _________________________\n";
print "Total SID Count              : ".sprintf("%*d",6,$sidCount)."\n";
print "Slow Query Count (>=10 secs) : ".sprintf("%*d",6,$slowcount)."     (".sprintf("%*.2f",5,$slowcount*100/$sidCount)."%)\n";
print "Total Duration (secs)        : ".sprintf("%*.3f",10,$stat->sum())."\n";
print "Mean Duration/SID            : ".sprintf("%*.3f",10,$stat->mean())."\n";#$avgTime\n";
print "Mode Duration/SID            : ".sprintf("%*.3f",10,$stat->mode())."\n";#$avgTime\n";
print "Minimum Duration             : ".sprintf("%*.3f",10,$stat->min())." (sid: ".@sids[$stat->mindex()].")\n";
print "Maximum Duration             : ".sprintf("%*.3f",10,$stat->max())." (sid: ".@sids[$stat->maxdex()].")\n";
print "Duration Standard Deviation  : ".sprintf("%*.3f",10,$stat->standard_deviation())."\n";

my $i=0;
print "Duration (secs) Bins         :- \n";
foreach (@interval) {
    $slot = $_;
    print sprintf("%*s",15,"[$i,$slot) : ").sprintf("%*d",6,$timeBucketCount{$slot})." (".sprintf("%*.2f",5,$timeBucketCount{$slot}*100/$sidCount)."%)\n";
    $i = $slot;
}
print sprintf("%*s",15,"[$i,+inf) : ").sprintf("%*d",6,$timeBucketCount{9999})." (".sprintf("%*.2f",5,$timeBucketCount{9999}*100/$sidCount)."%)\n";

print "\nHourly Distribution          :-  avg time     (  reqs)      (   qps)\n";
my $maxqps = 0;
my $avgqps = 0;
my $i = 0;
foreach(@keys) {
  $key = $_;
  $i++;
  my $qps = $totalReqs{$key}/$secs;
  if($maxqps < $qps) {
    $maxqps = $qps;
  }
  $avgqps += $qps;
  print(sprintf("%*s",28,$key)." : ".sprintf("%*.3f",10,$totalTime{$key}/$totalReqs{$key})."     (".sprintf("%*d",6,$totalReqs{$key}).")      (".sprintf("%*.2f",6,$qps).")\n");
}
$avgqps = $avgqps/$i;
print "\nAverage QPS: ".sprintf("%*.3f",10, $avgqps);
print "\nPeek QPS   : ".sprintf("%*.3f",10, $maxqps)."\n";

print "_______________________________________________________________\n";


my $countstat = Statistics::Descriptive::Full->new();
$countstat->add_data(values(%sidResCount));

print "\n_________________ Result Count Analysis _______________________\n";
print "Total SID Count                  : ".sprintf("%*d",12,$sidCount)."\n";
print "Total Result Count (secs)        : ".sprintf("%*.3f",16,$countstat->sum())."\n";
print "Mean Result Count/SID            : ".sprintf("%*.3f",16,$countstat->mean())."\n";#$avgTime\n";
print "Mode Result Count/SID            : ".sprintf("%*.3f",16,$countstat->mode())."\n";#$avgTime\n";
print "Minimum Result Count             : ".sprintf("%*.3f",16,$countstat->min())." (sid: ".@sids[$countstat->mindex()].")\n";
print "Maximum Result Count             : ".sprintf("%*.3f",16,$countstat->max())." (sid: ".@sids[$countstat->maxdex()].")\n";
print "Result Count Standard Deviation  : ".sprintf("%*.3f",16,$countstat->standard_deviation())."\n";

my $ci=0;
print "Result Count Bins         :- \n";
foreach (@countinterval) {
    $slot = $_;
    print sprintf("%*s",25,"[$ci,$slot) : ").sprintf("%*d",10,$countBucketCount{$slot})." (".sprintf("%*.2f",5,$countBucketCount{$slot}*100/$sidCount)."%)\n";
    $ci = $slot;
}
print sprintf("%*s",25,"[$ci,+inf) : ").sprintf("%*d",10,$countBucketCount{9999})." (".sprintf("%*.2f",5,$countBucketCount{9999}*100/$sidCount)."%)\n";

print "\nHourly Distribution          :-  avg count    (  reqs)\n";
foreach(@keys) {
  $key = $_;
  print(sprintf("%*s",28,$key)." : ".sprintf("%*.3f",11,$totalCount{$key}/$totalReqs{$key})."    (".sprintf("%*d",6,$totalReqs{$key}).")\n");
}
print "_______________________________________________________________\n";


