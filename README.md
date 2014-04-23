taverna-bioswr-perspective
==========================

_[BioSWR](http://inb.bsc.es/BioSWR/) Registry plug-in for
_[Taverna](http://www.taverna.org.uk/) Workbench.

(c) 2014 Spanish National Bioinformatics Institute (INB),
    Barcelona Supercomputing Center and The University of Manchester

Introduction
------------

BioSWR is a Semantic Web services Registry for Bioinformatics which uses 
_[WSDL 2.0: RDF mapping](http://www.w3.org/TR/wsdl20-rdf/) ontology as a model.

This plug-in allows Web services discovery and annotation. 
The selected services may be imported into Taverna Wokflow Designer.

**NB** Because BioSWR uses WSDL 2.0 model, the plug-in does not work with current 
Taverna code (T3 a2). It requieres a new, experimental, 'taverna-wsdl-*' 3.0 branch.
The WSDL HTTP (RESTful) services are imported via 'rest-activity-ui'.

