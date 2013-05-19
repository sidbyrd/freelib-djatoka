<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/djatokaViewer">
		<xsl:variable name="path" select="defaultPath" />

		<html>
			<head>
				<link rel="stylesheet" type="text/css" href="/css/view.css" />
				<link rel="stylesheet" type="text/css" href="/css/lightbox.css" media="screen" />
				
				<script type="text/javascript" src="/javascript/openseadragon.js">&#xa0;</script>
				<script type="text/javascript" src="/javascript/djtilesource.js">&#xa0;</script>
				<script type="text/javascript" src="/javascript/prototype.js">&#xa0;</script>
				<script type="text/javascript" src="/javascript/scriptaculous.js?load=effects,builder">&#xa0;</script>
				<script type="text/javascript" src="/javascript/lightbox.js">&#xa0;</script>
				<script type="text/javascript" src="/javascript/zoomview.js">&#xa0;</script>
				
				<title>Image Navigator</title>
			</head>
			<body>
				<div id="header">Image Navigator</div>
				<div id="totals">
					<xsl:if test="tifStats/@totalSize != 'null'">
						<span class="bold">TIFFs: </span>
						<xsl:value-of select="tifStats/@fileCount" />
						files
						<xsl:text>(</xsl:text>
						<xsl:value-of select="tifStats/@totalSize" />
						<xsl:text>)</xsl:text>
						~
						<span class="bold">JP2s: </span>
						<xsl:value-of select="jp2Stats/@fileCount" />
						files
						<xsl:text>(</xsl:text>
						<xsl:value-of select="jp2Stats/@totalSize" />
						<xsl:text>)</xsl:text>
					</xsl:if>
				</div>
				<div id="breadcrumbs">
					Path: /
					<a href="{concat($path, '/')}">ROOT</a>
					<xsl:for-each select="path/part">
						<text> / </text>
						<xsl:value-of select="." />
					</xsl:for-each>
				</div>
				<div id="folder">
					<xsl:for-each select="dir">
						<div class="folder">
							<a href="{concat(./@name, '/')}" title="{./@name}">
								<img src="/images/folder.png" width="75%" alt="{./@name}" />
								<xsl:choose>
									<xsl:when test="string-length(./@name) &gt; 11">
										<xsl:value-of select="concat(substring(./@name, 0, 11), '...')" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="./@name" />
									</xsl:otherwise>
								</xsl:choose>
							</a>
						</div>
					</xsl:for-each>
				</div>
				<div id="image">
					<xsl:for-each select="file">
						<xsl:variable name="fileName">
							<xsl:choose>
								<xsl:when test="substring(@name, string-length(@name) - 3) = '.jp2'">
									<xsl:value-of select="substring(@name, 0, string-length(@name) - 3)"/>
								</xsl:when>
								<xsl:when test="substring(@name, string-length(@name) - 3) = '.j2k'">
									<xsl:value-of select="substring(@name, 0, string-length(@name) - 3)"/>
								</xsl:when>
							</xsl:choose>
						</xsl:variable>
						<xsl:variable name="zoomLink">
							<text>&lt;a href=&quot;#&quot; onclick=&quot;zoom(&apos;</text>
							<xsl:value-of select="$fileName"/>
							<text>&apos;);&quot;&gt;Zoom In!&lt;/a&gt;</text>
						</xsl:variable>

						<div class="image">
							<a href="/view/image/{$fileName}" rel="lightbox[thumbs]"
								title="{$zoomLink}">
								<img src="/view/thumbnail/{$fileName}" />
							</a>
						</div>
					</xsl:for-each>
				</div>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>