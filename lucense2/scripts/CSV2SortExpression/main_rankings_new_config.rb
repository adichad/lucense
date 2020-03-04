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
    "Full Query Any Order Match",
    "Partial Query Match",
    "No Match"
],
  "vals"=>[
    "([bool]_isfullexact_geo_path_aliases_||[bool]_isfullexact_name@geo_path_aliases_||[bool]_isfullexact_aliases@geo_path_aliases_)",
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
}
]

