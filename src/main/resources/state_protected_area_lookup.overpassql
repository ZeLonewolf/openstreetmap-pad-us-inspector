[timeout:180][out:json];
area($AREA)->.searchArea;
(
  nwr["boundary"]["iucn_level"~"^(IA|IB|II|III|IV|V|VI)$"](area.searchArea);
  nwr["boundary"]["protect_class"~"^(1|1a|1b|2|3|4|5|6)$"](area.searchArea);
);
out tags;
