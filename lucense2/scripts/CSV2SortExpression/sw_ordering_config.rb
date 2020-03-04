@@vars = [
  {
  "keys"=>[
    "Yes",
    "No"
],
  "vals"=>[
    "([int]_boostminpos_gp_aliases_boosted_>0)",
    "true"
]
},
  {
  "keys"=>[
    "Full Name Full Query Any Order Match",
    "Full Query Any Order Match",
    "Partial Query Match",
    "No Match"
],
  "vals"=>[
    "([int]_lcslen_name_==[int]_fieldnorm_name_||[bool]_isfullexact_aliases_)&&[int]_numwords_geo_path_aliases_>0&&[bool]_isfullexact_name@aliases@geo_path_aliases_",
    "([bool]_isfullexact_name@aliases@geo_path_aliases_)",
    "([int]_numwords_geo_path_aliases_>0)",
    "true"
]
},
  {
  "keys"=>[
    "Full Query Any Order Match",
    "Partial Query Match",
    "No Match"
],
  "vals"=>[
    "([bool]_isfullexact_place_type_||[bool]_isfullexact_themes_)",
    "([int]_numwords_place_type_>0||[int]_numwords_themes_>0)",
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
    "([int]guide_word_count>150)",
    "true"
]
},
  {
  "keys"=>[
    "Full In Order Match", 
    "Full Query In Order Match",
    "Full Any Order Match",
    "Full Query Any Order Match",
    "Partial Query Match",
    "No Match"
],
  "vals"=>[
    "([bool]_isexact_name_&&[int]_fieldnorm_name_==[int]_numwords_name_)||([bool]_isfullexact_aliases_)",
    "([bool]_isexact_name_||[bool]_isexact_aliases_)",
    "([bool]_isall_name_&&[int]_fieldnorm_name_==[int]_numwords_name_)||([bool]_isall_aliases_&&[int]_fieldnorm_aliases_==[int]_numwords_aliases_)",
    "([bool]_isall_name_||[bool]_isall_aliases_)",
    "([int]_numwords_name_>0||[int]_numwords_aliases_>0)",
    "true"
]
}
]

