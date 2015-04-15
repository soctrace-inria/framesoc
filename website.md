# Framesoc

[[ http://soctrace-inria.github.io/framesoc/images/framesoc.png | width = 630px ]]

Framesoc is a generic trace management and analysis infrastructure.

## Install Framesoc
### Install Eclipse plugin version
To easily start playing with Framesoc, you can install its Eclipse plugin version.

- First, [download](https://www.eclipse.org/downloads/) and extract Eclipse (**version 4.3 or higher**).
- Then, launch Eclipse and go to *Help > Install New Software*. 
- In the new dialog, put the following URL in the field *Work with*:
  http://soctrace-inria.github.io/updatesite/
- Press *Enter*. Framesoc, Framesoc Importers and other Framesoc tools should be available for installation. 
- Select at least Framesoc and Framesoc Importers, then click on *Next*, *Next*, accept the license agreement, 
  and finally click on *Finish*.

### Get the Sources
Framesoc source code is available on [GitHub](https://github.com/soctrace-inria/framesoc).

If you want to setup a development environment to hack the code, follow 
[this procedure](https://github.com/soctrace-inria/framesoc/wiki/Framesoc-Eclipse-Plugin-Development-Environment-Setup).

## User and Developer Guide
The Framesoc User Guide can be downloaded [here](https://github.com/soctrace-inria/framesoc/blob/master/src/fr.inria.soctrace.maven.repository/archive/doc/framesoc_user_guide.pdf?raw=true).

The Framesoc GitHub wiki pages are available [here](https://github.com/soctrace-inria/framesoc/wiki/).

## Supported Trace Formats
We provide [importers](https://github.com/soctrace-inria/framesoc.importers)
(installed automatically with the plugin version) for the following trace formats:
- Pajé Dump
- Pajé (only on linux)
- CTF
- OTF2 (only on linux)
- Paraver (experimental)
- GSTreamer

## Test Traces
You can start playing with Framesoc using the following test traces.

- Pajé Dump Scorep Trace

  [![DOI](https://zenodo.org/badge/doi/10.5281/zenodo.15989.svg)](http://dx.doi.org/10.5281/zenodo.15989)

  This trace can be imported using the Pajé Dump Importer (default settings).
  
- CTF Kernel Trace

  [![DOI](https://zenodo.org/badge/doi/10.5281/zenodo.16026.svg)](http://dx.doi.org/10.5281/zenodo.16026)

  This trace can be imported extracting the archive, then using the CTFTrace Importer. 
  This trace has been provided by the [Lttng project](http://lttng.org/).

You can import traces using the following procedure:
- Go to *Framesoc > Trace Analysis > Import Trace*.
- Select the right importer, according to the trace.

## External modules
- [Ocelotl](http://soctrace-inria.github.io/ocelotl/), 
  an analysis tool providing multidimensional overviews for huge traces.
- [Framesoc Importers](https://github.com/soctrace-inria/framesoc.importers), 
  trace importers for several trace formats.

## Support and Contact

You can contact us by email (replace -at- with @):

- generoso.pagano -at- inria.fr
- damien.dosimont -at- imag.fr
- youenn.corre -at- inria.fr

Follow us on [GitHub](https://github.com/soctrace-inria/framesoc)!

## License

Framesoc is based on the Eclipse framework and it is released under 
the [EPL (Eclipse Public License) v 1.0](https://www.eclipse.org/legal/epl-v10.html).
The legal documentation has been written following the guidelines 
specified [here](http://www.eclipse.org/legal/guidetolegaldoc.php).
