package archix_base.organization.entity;

import archix_base.document.entity.Document;
import archix_base.document.entity.Resource;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;














@Entity
@Table(name = "namespace")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Namespace extends Resource {

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resource> children = new ArrayList<>();

    public void addChild(Resource resource) {
        children.add(resource);
        resource.setParent(this);
    }

    public void removeChild(Resource resource) {
        children.remove(resource);
        resource.setParent(null);
    }

    public List<Document> getDocuments() {
        List<Document> docs = new ArrayList<>();
        for (Resource r : children) {
            if (r instanceof Document)
                docs.add((Document) r);
        }
        return docs;
    }

    public List<Namespace> getNamespaces() {
        List<Namespace> nss = new ArrayList<>();
        for (Resource r : children) {
            if (r instanceof Namespace)
                nss.add((Namespace) r);
        }
        return nss;
    }
}








