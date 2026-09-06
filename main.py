import urllib.request
import xml.etree.ElementTree as ET

def define_env(env):
    @env.macro
    def library_version():
        try:
            url = "https://repo1.maven.org/maven2/io/github/dp-hridayan/compose-settings-graph/maven-metadata.xml"
            req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
            with urllib.request.urlopen(req, timeout=5) as response:
                xml_data = response.read()
            root = ET.fromstring(xml_data)
            version = root.find(".//release")
            if version is not None and version.text:
                return version.text
        except Exception:
            pass
        return "1.0.0"
