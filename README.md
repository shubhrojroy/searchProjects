# Similar
In the area of Content Discovery, finding similar or related files is an often explored task. There are quite a few open-source libraries such as text2vec , gensim etc. that provide such functionalities. These may be great for prototyping or POC purposes but not scalable for production usecases. Functionalities present in Lucene or python libraries are not usable directly out-of-the-box and hence require significant development effort. Finally other services such as IBM Watson may be too expensive to engage with at the outset.
Similar is an open-source scala library implemented on top of Lucene to provide out-of-the box document similarity functionality. The library was designed keeping scalability and extensibility for large scale production applications in mind. 

Some of the key features of this library are:
- Cosine distance based similarity using tf-idf vectors with ability to use other distance measures.
- In-build clustering mechanism that precomputes similarity clusters off-line / at index time to avoid large number of computations at query time.
- Uses Latent Semantic Indexing (LSI) to reduce dimensionality of document vectors thereby significantly reducing similarity computation time as well as noise.

The library has been tested with Reuter's RCV1 and 20 Newsgroups datasets. On the 20 newsgroups dataset Similar attained a precision of 78% and recall of 82.02%.
