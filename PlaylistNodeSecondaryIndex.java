import java.util.ArrayList;

public class PlaylistNodeSecondaryIndex extends PlaylistNode {
	private ArrayList<String> genres;
	private ArrayList<PlaylistNode> children;

	public PlaylistNodeSecondaryIndex(PlaylistNode parent) {
		super(parent);
		genres = new ArrayList<String>();
		children = new ArrayList<PlaylistNode>();
		this.type = PlaylistNodeType.Internal;
	}
	
	public PlaylistNodeSecondaryIndex(PlaylistNode parent, ArrayList<String> genres, ArrayList<PlaylistNode> children) {
		super(parent);
		this.genres = genres;
		this.children = children;
		this.type = PlaylistNodeType.Internal;
	}
	
	// GUI Methods - Do not modify
	public ArrayList<PlaylistNode> getAllChildren()
	{
		return this.children;
	}
	
	public PlaylistNode getChildrenAt(Integer index) {
		
		return this.children.get(index);
	}
	

	public Integer genreCount()
	{
		return this.genres.size();
	}
	
	public String genreAtIndex(Integer index) {
		if(index >= this.genreCount() || index < 0) {
			return "Not Valid Index!!!";
		}
		else {
			return this.genres.get(index);
		}
	}
	
	
	// Extra functions if needed
	public void addChild(PlaylistNode child) {
		this.children.add(child);
	}

	public void addChild(Integer index, PlaylistNode child) {
		this.children.add(index, child);
	}

	public void addGenre(String genre) {
		this.genres.add(genre);
	}

	public ArrayList<String> getAllGenres() {
		return this.genres;
	}

	public void removeChild(int index) {
		this.children.remove(index);
	}

	public void setGenres(ArrayList<String> givenGenres) {
		this.genres = givenGenres;
	}

	public void setChildren(ArrayList<PlaylistNode> givenChildren) {
		this.children = givenChildren;
	}

	public int getChildrenCount() {
		return this.children.size();
	}


}
