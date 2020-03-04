@@vars = [
  {
  "keys"=>[
    "Full In Order Match", 
    "Full Name Full Query In Order Match",
    "Full Query In Order Match",
    "Full Any Order Match",
    "Full Query Any Order Match",
    "Partial Query Match",
    "No Match"
],
  "vals"=>[
    "([bool]_isfullexact_name_||[bool]_isfullexact_aliases_)",
    "([bool]_isfullexact_name@geo_path_aliases_||[bool]_isfullexact_aliases@geo_path_aliases_)",
    "([bool]_isexact_name_||[bool]_isexact_aliases_)",
    "([bool]_isall_name_&&[int]_fieldnorm_name_==[int]_numwords_name_)",
    "([bool]_isall_name_||[bool]_isall_aliases_)",
    "([int]_numwords_name_>0||[int]_numwords_aliases_>0)",
    "true"
]
},
  {
  "keys"=>[
    "Yes",
    "No"
],
  "vals"=>[
    "([int]poi_rank>0 || [int]child_count>5000)",
    "true"
]
},
  {
  "keys"=>[
    "Yes",
    "No"
],
  "vals"=>[
    "([int]guide_word_count>151)",
    "true"
]
}
]

