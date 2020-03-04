#!/usr/bin/perl

use HTTP::Date;
use POSIX qw(strftime);
use LWP::Simple;
use Statistics::Descriptive;
use strict;

my $dateToday = strftime "%Y-%m-%d", localtime;

my $logFile = $ARGV[0]; 

chomp $logFile;


my %typeCount;
my %levelCount;
my %hourlyTotalCount;
my %hourlyCount;

#[2011 Jun 03 Fri 09:40:35.498] [ERROR] [lucense.request.SearchRequest:660] org.apache.lucene.queryParser.ParseException: Cannot parse '': Encountered "<EOF>" at line 1, column 0
my $we = qr/^\[([^\]]+)\] \[\s*(ERROR|FATAL|WARN|INFO)\] \[([^\]]+)\] ([^:\s]+).*/;

my  $stamp;
my  $location;
my  $level;
my  $app;
my  $errCount;
my  $type;
my  $timeStampHourly;
my  $maxTypeBreakup = 4;
print "analysing $logFile...\n";

open FILE, "<$logFile";
$errCount=0;
while ( <FILE> ) {
  if(m/FATAL|ERROR|WARN|INFO/) {
    chomp;
    if($_ eq "") {next;}
    m/$we/;
    #print $_;
    
    $stamp       = $1;
    $level       = $2;
    $location    = $3;
    $type        = $4;
    $errCount++;
   
#    print "$1, $2, $3, $4\n";
    $typeCount{$type}++;
    $levelCount{$level}++;
    $timeStampHourly = $stamp;
    $timeStampHourly =~ s/([^:]+).*/$1/i;
    $hourlyTotalCount{$timeStampHourly}++;
    $hourlyCount{$timeStampHourly}{$type}++;
  }
}
close FILE;




print "\n_______________________ Error Log Analysis __________________________\n";
print "Total Error Count             : ".sprintf("%*d",6,$errCount)."\n";

my @topTypes = sort { $typeCount{$b} <=> $typeCount{$a} } keys %typeCount;
print "\nErrors by Type                :- \n";
foreach(@topTypes) {
  my $type = $_;
  my $count = $typeCount{$type};
  print sprintf("%*s",80,"$type : ").sprintf("%*d",6,$count)." (".sprintf("%*.2f",6,$count*100/$errCount)."%)"."\n";
}
my @topLevels = sort { $levelCount{$b} <=> $levelCount{$a} } keys %levelCount;
print "\nErrors by Level               :- \n";
foreach(@topLevels) {
  my $level = $_;
  my $count = $levelCount{$level};
  print sprintf("%*s",32,"$level : ").sprintf("%*d",6,$count)." (".sprintf("%*.2f",6,$count*100/$errCount)."%)"."\n";
}
my $n = keys(@topTypes)-1;
$n = ($maxTypeBreakup-1)<$n?($maxTypeBreakup-1):$n;
@topTypes = @topTypes[0..$n];
print "\nHourly Errors by Type         :            TOTAL";
foreach(@topTypes) {
  my $full = $_;
  $full =~ m/(?:.*\.)*(.*)/;
  $full = $1;
  my $short = "";
  while($full =~ m/(?:([A-Z])[^A-Z]*)/g) {
    $short .= $1;
  }
  print sprintf("%*s",16,$short); 
}
print("\n");
my @topHours = sort { $a cmp $b } keys %hourlyTotalCount;
foreach(@topHours) {
  my $hour = $_;
  my $count = $hourlyTotalCount{$_};
  print sprintf("%*s",32,"$hour : ").sprintf("%*d",6,$count)." (".sprintf("%*.2f",6,$count*100/$errCount)."%)";
  foreach(@topTypes) {
    $type = $_;
    print sprintf("%*d", 6, $hourlyCount{$hour}{$type})." (".sprintf("%*.2f",6,$hourlyCount{$hour}{$type}*100/$errCount)."%)";
  }
  print "\n";
}

print "_____________________________________________________________________\n";

