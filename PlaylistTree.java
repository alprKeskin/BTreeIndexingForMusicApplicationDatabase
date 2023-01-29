import java.util.ArrayList;
import java.util.Optional;

public class PlaylistTree {
	
	public PlaylistNode primaryRoot;		// root of the primary B+ tree
	public PlaylistNode secondaryRoot;	// root of the secondary B+ tree
	public PlaylistTree(Integer order) {
		PlaylistNode.order = order;
		primaryRoot = new PlaylistNodePrimaryLeaf(null);
		primaryRoot.level = 0;
		secondaryRoot = new PlaylistNodeSecondaryLeaf(null);
		secondaryRoot.level = 0;
	}
	
	public void addSong(CengSong song) {
		// TODO: Implement this method
		// add methods to fill both primary and secondary tree

		// update the primary tree
		addSongForPrimaryTree(song);
		// update the secondary tree
		addSongForSecondaryTree(song);

		return;
	}

	private void addSongForPrimaryTree(CengSong song) {
		// get the leaf to insert the new song
		PlaylistNodePrimaryLeaf leafToInsertInto = getLeafToInsertInto(this.primaryRoot, song.audioId());

		// get the correct index to insert the song into the leaf we have just got
		int indexToInsertIntoTheLeaf = getIndexToInsert(leafToInsertInto, song.audioId());

		// if an overflow will not occur
		if (leafToInsertInto.songCount() + 1 <= 2 * PlaylistNode.order) {
			// add the song to the leaf that you have found
			leafToInsertInto.addSong(indexToInsertIntoTheLeaf, song);
			// terminate the insertion
			return;
		}

		// if an overflow occurs in the leaf
		handleOverflow(leafToInsertInto, song);
	}

	private PlaylistNodePrimaryLeaf getLeafToInsertInto(PlaylistNode startingNode, Integer key) {
		// base case
		if (startingNode.type == PlaylistNodeType.Leaf) {
			return (PlaylistNodePrimaryLeaf) startingNode;
		}
		int i;
		// recursive case
		for (i = 0; i < ((PlaylistNodePrimaryIndex) startingNode).audioIdCount(); i++) {
			if (key < ((PlaylistNodePrimaryIndex) startingNode).audioIdAtIndex(i)) {
				return getLeafToInsertInto( ((PlaylistNodePrimaryIndex) startingNode).getChildrenAt(i), key);
			}
		}
		// if the key is greater than all ıd's in the current node
		return getLeafToInsertInto(((PlaylistNodePrimaryIndex) startingNode).getChildrenAt(i), key);
	}

	private int getIndexToInsert(PlaylistNodePrimaryLeaf leaf, Integer key) {
		// for all songs of the leaf
		for (int i = 0; i < leaf.songCount(); i++) {
			// if the key is less than current song id (cannot be equal to any song id)
			if (key < leaf.songAtIndex(i).audioId()) {
				// then we need to insert the song to this index
				return i;
			}
		}
		// if the key is greater than all id's in the given leaf
		return leaf.songCount();
	}

	private int getIndexToInsert(PlaylistNodePrimaryIndex node, Integer key) {
		// for all keys of the node
		for (int i = 0; i < node.audioIdCount(); i++) {
			// if the key is less than the current song id (cannot be equal to any song id)
			if (key < node.audioIdAtIndex(i)) {
				// then, we need to insert the key to this index
				return i;
			}
		}
		// if the key is greater than all keys in the given node
		return node.audioIdCount();
	}

	private void handleOverflow(PlaylistNodePrimaryLeaf overflowedLeaf, CengSong song) {
		// get the song count of the overflowed leaf
		int songCountOfOverflowedLeaf = overflowedLeaf.songCount();

		// get all the songs of the overflowed leaf
		ArrayList<CengSong> overflowedLeafSongs = new ArrayList<CengSong>(overflowedLeaf.getSongs());

		boolean flag = false;
		// add the song to this list into the correct index
		for (int i = 0; i < songCountOfOverflowedLeaf; i++) {
			if (song.audioId() < overflowedLeafSongs.get(i).audioId()) {
				overflowedLeafSongs.add(i, song);
				songCountOfOverflowedLeaf++;
				flag = true;
				break;
			}
		}
		if (!flag) {
			overflowedLeafSongs.add(song);
			songCountOfOverflowedLeaf++;
		}

		// get the parent of the overflowed node
		PlaylistNodePrimaryIndex parentNode = (PlaylistNodePrimaryIndex) overflowedLeaf.getParent();

		// if the parent is null (the leaf is the primary root)
		if (parentNode == null) {
			// create a new node to be parent (new primary root)
			PlaylistNodePrimaryIndex newParentNode = new PlaylistNodePrimaryIndex(null);

			// create a new leaf instead of overflowed leaf
			PlaylistNodePrimaryLeaf newOverflowedLeaf = new PlaylistNodePrimaryLeaf(newParentNode);
			// create a new leaf for the right side of the overflowed leaf which has the same parent
			PlaylistNodePrimaryLeaf newLeaf = new PlaylistNodePrimaryLeaf(newParentNode);

			// add these leaves as children of the parent node
			newParentNode.addChild(newOverflowedLeaf);
			newParentNode.addChild(newLeaf);


			// get the song id of the song located at the middle of the overflowed leaf
			int middleIndex = songCountOfOverflowedLeaf / 2;

			// move the right elements of the overflowed leaf to an arraylist
			for (int i = 0; i < songCountOfOverflowedLeaf; i++) {
				// if we are on the left side of the overflowed leaf
				if (i < middleIndex) {
					// copy the songs to the new overflowed leaf
					newOverflowedLeaf.addSong(i, overflowedLeafSongs.get(i));
				}
				else if (i == middleIndex) {
					// copy up the middle element to the parent node
					newParentNode.addAudioId(overflowedLeafSongs.get(i).audioId());
					// copy the song to the new leaf
					newLeaf.addSong(i - middleIndex, overflowedLeafSongs.get(i));
				}
				// if we are on the right side of the overflowed leaf
				else {
					// copy the songs to the new leaf
					newLeaf.addSong(i - middleIndex, overflowedLeafSongs.get(i));
				}
			}

			// update the primary root
			this.primaryRoot = newParentNode;

			// terminate the process
			return;
		}

		// get the audio id's of the parent node
		ArrayList<Integer> parentAudioIds = new ArrayList<Integer>(parentNode.getAllAudioIds());
		// get the children of the parent node
		// ArrayList<PlaylistNode> parentChildren = new ArrayList<PlaylistNode>(parentNode.getAllChildren());

		boolean willParentOverflowed = false;
		// if the parent node will be overflowed
		if (parentNode.audioIdCount() + 1 > 2 * PlaylistNode.order) {
			// mark the parent overflow information as true
			willParentOverflowed = true;
		}

		// create a new leaf instead of overflowed leaf
		PlaylistNodePrimaryLeaf newOverflowedLeaf = new PlaylistNodePrimaryLeaf(parentNode);
		// create a new leaf for the right side of the overflowed leaf which has the same parent
		PlaylistNodePrimaryLeaf newLeaf = new PlaylistNodePrimaryLeaf(parentNode);


		// get index of the overflowed leaf in the children of the parent
		int overflowedLeafIndexAsChild = getIndexToInsert(parentNode, overflowedLeaf.audioIdAtIndex(0));
		// remove overflowed leaf from the children of the parent
		parentNode.removeChild(overflowedLeafIndexAsChild);

		// get the song id of the song located at the middle of the overflowed leaf
		int middleIndex = songCountOfOverflowedLeaf / 2;

		// move the right elements of the overflowed leaf to the new leaf, and left elements of the overflowed leaf to the new overflowed leaf
		for (int i = 0; i < songCountOfOverflowedLeaf; i++) {
			// if we are on the left side of the overflowed leaf
			if (i < middleIndex) {
				// copy the songs to the new overflowed leaf
				newOverflowedLeaf.addSong(i, overflowedLeafSongs.get(i));
			}
			else if (i == middleIndex) {
				// get index to copy up the middle element
				int indexToCopyUp = getIndexToInsert(parentNode, overflowedLeafSongs.get(i).audioId());
				// copy up the middle element to the parent node (into the correct place)
				parentAudioIds.add(indexToCopyUp, overflowedLeafSongs.get(i).audioId()); // parentNode.addAudioId(indexToCopyUp, overflowedLeafSongs.get(i).audioId());
				// copy the song to the new leaf
				newLeaf.addSong(i - middleIndex, overflowedLeafSongs.get(i));
			}
			// if we are on the right side of the overflowed leaf
			else {
				// copy the songs to the new leaf
				newLeaf.addSong(i - middleIndex, overflowedLeafSongs.get(i));
			}
		}

		// if there will be no overflow in the parent node
		if (!willParentOverflowed) {
			// update the parent node's audio id's
			parentNode.setAudioIds(parentAudioIds);
			// insert the new leaf as child
			parentNode.addChild(overflowedLeafIndexAsChild, newLeaf);
			// insert the new overflowed leaf as child
			parentNode.addChild(overflowedLeafIndexAsChild, newOverflowedLeaf);
			// terminate the process
			return;
		}
		// if there will be an overflow in the parent node
		// test
		ArrayList<PlaylistNode> overflowedNodeChildren = new ArrayList<PlaylistNode>(parentNode.getAllChildren());
		overflowedNodeChildren.add(overflowedLeafIndexAsChild, newLeaf);
		overflowedNodeChildren.add(overflowedLeafIndexAsChild, newOverflowedLeaf);
		// test

		handleOverflowFromNode(parentNode, parentAudioIds, overflowedNodeChildren);
		// some code here...

	}

	private void handleOverflowFromNode(PlaylistNodePrimaryIndex overFlowedNode, ArrayList<Integer> overflowedAudioIds, ArrayList<PlaylistNode> overflowedNodeChildren) {

		// base case
		// if this is the primary root
		if ((PlaylistNodePrimaryIndex) overFlowedNode.getParent() == null) {
			// create a new parent node
			PlaylistNodePrimaryIndex newParentNode = new PlaylistNodePrimaryIndex(null);
			// create a new overflowed node for the left side of the overflowed node
			PlaylistNodePrimaryIndex newOverflowedNode = new PlaylistNodePrimaryIndex(newParentNode);
			// create a new node for the right side of the overflowed node
			PlaylistNodePrimaryIndex newNode = new PlaylistNodePrimaryIndex(newParentNode);
			// set new overflowed node and new node as the children of the new parent node
			newParentNode.addChild(newOverflowedNode);
			newParentNode.addChild(newNode);
			// move up the middle element to the new parent node
			int middleIndex = overflowedAudioIds.size() / 2;
			newParentNode.addAudioId(overflowedAudioIds.get(middleIndex));
			// move the right audio id's of the overflowed node
			int i;
			for (i = 0; i < overflowedAudioIds.size(); i++) {
				if (i < middleIndex) {
					// add the audio id to the new overflowed node audio id's
					newOverflowedNode.addAudioId(overflowedAudioIds.get(i));
					// add the children to the overflowed node children
					newOverflowedNode.addChild(overflowedNodeChildren.get(i));
					// update the parent of the children to the new overflowed node
					overflowedNodeChildren.get(i).setParent(newOverflowedNode);
				}
				else if (i == middleIndex) {
					// add the children to the overflowed node children
					newOverflowedNode.addChild(overflowedNodeChildren.get(i));
					// update the parent of the children to the new overflowed node
					overflowedNodeChildren.get(i).setParent(newOverflowedNode);
				}
				else {
					// add the audio id to the new node audio id's
					newNode.addAudioId(overflowedAudioIds.get(i));
					// add the children to the new node children
					newNode.addChild(overflowedNodeChildren.get(i));
					// update the parent of the children to the new node
					overflowedNodeChildren.get(i).setParent(newNode);
				}
			}
			// add the last child to the new node children
			newNode.addChild(overflowedNodeChildren.get(i));
			// update the parent of the children to the new node
			overflowedNodeChildren.get(i).setParent(newNode);
			// remove the audio id
			overflowedAudioIds.remove(middleIndex);

			// update primary root such that it is new parent node
			this.primaryRoot = newParentNode;

			// terminate the process
			return;
		}


		// recursive case
		// get parent of the overflowed node
		PlaylistNodePrimaryIndex parentNode = (PlaylistNodePrimaryIndex) overFlowedNode.getParent();

		// if an overflow will occur in the parent node of the overflowed node
		boolean willParentOverflowed = false;
		// if the parent node will be overflowed
		if (parentNode.audioIdCount() + 1 > 2 * PlaylistNode.order) {
			// mark the parent overflow information as true
			willParentOverflowed = true;
		}

		// get the keys of the parent node
		ArrayList<Integer> parentNodeKeys = new ArrayList<Integer>(parentNode.getAllAudioIds());
		// get the children of the parent node
		ArrayList<PlaylistNode> parentNodeChildren = new ArrayList<PlaylistNode>(parentNode.getAllChildren());

		// create a new left node for the left side of the overflowed node
		PlaylistNodePrimaryIndex newLeftNode = new PlaylistNodePrimaryIndex(parentNode);
		// create a new node for the right side of the overflowed node
		PlaylistNodePrimaryIndex newRightNode = new PlaylistNodePrimaryIndex(parentNode);

		// get the index of the overflowed node as the child of the parent node
		int overflowedNodeIndexAsChild = getIndexToInsert(parentNode, overFlowedNode.audioIdAtIndex(0));
		// remove the overflowed node from parent node children
		parentNodeChildren.remove(overflowedNodeIndexAsChild);
		// insert the new right node as a child of the parent
		parentNodeChildren.add(overflowedNodeIndexAsChild, newRightNode);
		// insert the new left node as a child of the parent
		parentNodeChildren.add(overflowedNodeIndexAsChild, newLeftNode);

		// get the middle index
		int middleIndex = overflowedAudioIds.size() / 2;

		int i;
		for (i = 0; i < overflowedAudioIds.size(); i++) {
			if (i < middleIndex) {
				// add the key to the new left node keys
				newLeftNode.addAudioId(overflowedAudioIds.get(i));
				// add the children to the overflowed node children
				newLeftNode.addChild(overflowedNodeChildren.get(i));
				// update the parent of the children to the new overflowed node
				overflowedNodeChildren.get(i).setParent(newLeftNode);
			}
			else if (i == middleIndex) {
				// add the children to the overflowed node children
				newLeftNode.addChild(overflowedNodeChildren.get(i));
				// update the parent of the children to the new left node
				overflowedNodeChildren.get(i).setParent(newLeftNode);
			}
			else {
				// add the audio id to the new right node audio id's
				newRightNode.addAudioId(overflowedAudioIds.get(i));
				// add the children to the new node children
				newRightNode.addChild(overflowedNodeChildren.get(i));
				// update the parent of the children to the new node
				overflowedNodeChildren.get(i).setParent(newRightNode);
			}
		}
		// add the last child to the new node children
		newRightNode.addChild(overflowedNodeChildren.get(i));
		// update the parent of the children to the new right node
		overflowedNodeChildren.get(i).setParent(newRightNode);

		// find the correct index to move up the middle element
		int middleElementInsertionIndex = getIndexToInsert(parentNode, overflowedAudioIds.get(middleIndex));
		// move up the middle element to the new parent node
		parentNodeKeys.add(middleElementInsertionIndex, overflowedAudioIds.get(middleIndex));
		// remove the middle key
		overflowedAudioIds.remove(middleIndex);

		// if no overflow will occur
		if (!willParentOverflowed) {
			parentNode.setAudioIds(parentNodeKeys);
			parentNode.setChildren(parentNodeChildren);
			return;
		}

		ArrayList<Integer> parentNodeKeysCopy = new ArrayList<Integer>(parentNodeKeys);
		ArrayList<PlaylistNode> parentNodeChildrenCopy = new ArrayList<PlaylistNode>(parentNodeChildren);

		// if an overflow in the parent node will occur
		handleOverflowFromNode(parentNode, parentNodeKeysCopy, parentNodeChildrenCopy);

		parentNode.setAudioIds(parentNodeKeys);
		parentNode.setChildren(parentNodeChildren);
		return;
	}













	private void addSongForSecondaryTree(CengSong song) {
		// get the leaf to insert the new song
		PlaylistNodeSecondaryLeaf leafToInsertInto = getLeafToInsertInto(this.secondaryRoot, song.genre());

		// get the correct index to insert the song into the leaf we have just got
		int indexToInsertIntoTheLeaf = getIndexToInsert(leafToInsertInto, song.genre());

		// not a new genre
		if (indexToInsertIntoTheLeaf == leafToInsertInto.genreCount() + 1) {
			leafToInsertInto.addSong(0, song);
			return;
		}

		if (indexToInsertIntoTheLeaf < 0) {
			leafToInsertInto.addSong(-indexToInsertIntoTheLeaf, song);
			return;
		}

		// if an overflow will not occur
		if (leafToInsertInto.genreCount() + 1 <= 2 * PlaylistNode.order) {
			// add the song to the leaf that you have found
			leafToInsertInto.addSong(indexToInsertIntoTheLeaf, song);
			// terminate the insertion
			return;
		}

		// if an overflow occurs in the leaf
		handleOverflow(leafToInsertInto, song);
	}

	private PlaylistNodeSecondaryLeaf getLeafToInsertInto(PlaylistNode startingNode, String genre) {
		// base case
		if (startingNode.type == PlaylistNodeType.Leaf) {
			return (PlaylistNodeSecondaryLeaf) startingNode;
		}
		int i;
		// recursive case
		for (i = 0; i < ((PlaylistNodeSecondaryIndex) startingNode).genreCount(); i++) {
			// if the given genre is less than the genre in the index
			if (genre.compareTo( ((PlaylistNodeSecondaryIndex) startingNode).genreAtIndex(i) ) < 0) {
				return getLeafToInsertInto( ((PlaylistNodeSecondaryIndex) startingNode).getChildrenAt(i), genre);
			}
			// if the given genre is greater or equal to the genre in the index, continue
		}
		// if the given genre is greater than all genres in the current node
		return getLeafToInsertInto(((PlaylistNodeSecondaryIndex) startingNode).getChildrenAt(i), genre);
	}


	private int getIndexToInsert(PlaylistNodeSecondaryLeaf leaf, String genre) {
		// for all songs of the leaf
		for (int i = 0; i < leaf.genreCount(); i++) {
			// if the genre is already in the leaf
			if (genre.compareTo(leaf.genreAtIndex(i)) == 0) {
				// yeni bir genre değil!
				if (i == 0) {
					return leaf.genreCount() + 1;
				}
				return -i;
			}
			// if the given genre is less than current song genre (can be equal to any song id)
			if (genre.compareTo(leaf.genreAtIndex(i)) < 0) {
				// then we need to insert the song to this index
				return i;
			}
		}
		// if the key is greater than all id's in the given leaf
		return leaf.genreCount();
	}

	private int getIndexToInsert(PlaylistNodeSecondaryIndex node, String genre) {
		// for all songs of the leaf
		for (int i = 0; i < node.genreCount(); i++) {
			// if the genre is already in the leaf
//			if (genre.compareTo(node.genreAtIndex(i)) == 0) {
//				return -1;
//			}
			// if the given genre is less than current song genre (can be equal to any song id)
			if (genre.compareTo(node.genreAtIndex(i)) < 0) {
				// then we need to insert the song to this index
				return i;
			}
		}
		// if the key is greater than all id's in the given leaf
		return node.genreCount();
	}


	private void handleOverflow(PlaylistNodeSecondaryLeaf overflowedLeaf, CengSong song) {
		// get the song count of the overflowed leaf
		int songCountOfOverflowedLeaf = overflowedLeaf.genreCount();

		// get all the songs of the overflowed leaf
		ArrayList<ArrayList<CengSong>> overflowedLeafSongs = new ArrayList<ArrayList<CengSong>>(overflowedLeaf.getSongBucket());

		boolean flag = false;
		// add the song to this list into the correct index
		for (int i = 0; i < songCountOfOverflowedLeaf; i++) {
//			if (song.genre().compareTo(overflowedLeaf.genreAtIndex(i)) == 0) {
//				overflowedLeafSongs.get(i).add(song);
//				// code...............................................
//			}
			if (song.genre().compareTo(overflowedLeaf.genreAtIndex(i)) < 0) {
				ArrayList<CengSong> temp = new ArrayList<CengSong>();
				temp.add(song);
				overflowedLeafSongs.add(i, temp);
				songCountOfOverflowedLeaf++;
				flag = true;
				break;
			}
		}
		if (!flag) {
			ArrayList<CengSong> temp = new ArrayList<CengSong>();
			temp.add(song);
			overflowedLeafSongs.add(temp);
			songCountOfOverflowedLeaf++;
		}

		// get the parent of the overflowed node
		PlaylistNodeSecondaryIndex parentNode = (PlaylistNodeSecondaryIndex) overflowedLeaf.getParent();

		// if the parent is null (the leaf is the primary root)
		if (parentNode == null) {
			// create a new node to be parent (new primary root)
			PlaylistNodeSecondaryIndex newParentNode = new PlaylistNodeSecondaryIndex(null);

			// create a new leaf instead of overflowed leaf
			PlaylistNodeSecondaryLeaf newOverflowedLeaf = new PlaylistNodeSecondaryLeaf(newParentNode);
			// create a new leaf for the right side of the overflowed leaf which has the same parent
			PlaylistNodeSecondaryLeaf newLeaf = new PlaylistNodeSecondaryLeaf(newParentNode);

			// add these leaves as children of the parent node
			newParentNode.addChild(newOverflowedLeaf);
			newParentNode.addChild(newLeaf);


			// get the song id of the song located at the middle of the overflowed leaf
			int middleIndex = songCountOfOverflowedLeaf / 2;

			// move the right elements of the overflowed leaf to an arraylist
			for (int i = 0; i < songCountOfOverflowedLeaf; i++) {
				// if we are on the left side of the overflowed leaf
				if (i < middleIndex) {
					// copy the songs to the new overflowed leaf
					newOverflowedLeaf.getSongBucket().add(i, overflowedLeafSongs.get(i));
				}
				else if (i == middleIndex) {
					// copy up the middle element to the parent node
					if (overflowedLeafSongs.get(i).size() == 0) {
						System.out.println("ERROR 0");
						return;
					}
					newParentNode.addGenre(overflowedLeafSongs.get(i).get(0).genre());
					// copy the song to the new leaf
					newLeaf.getSongBucket().add(i - middleIndex, overflowedLeafSongs.get(i));
				}
				// if we are on the right side of the overflowed leaf
				else {
					// copy the songs to the new leaf
					newLeaf.getSongBucket().add(i - middleIndex, overflowedLeafSongs.get(i));
				}
			}

			// update the primary root
			this.secondaryRoot = newParentNode;

			// terminate the process
			return;
		}
		// we are done up to this point


		// get the audio id's of the parent node
		ArrayList<String> parentGenres = new ArrayList<String>(parentNode.getAllGenres());
		// get the children of the parent node
		// ArrayList<PlaylistNode> parentChildren = new ArrayList<PlaylistNode>(parentNode.getAllChildren());

		boolean willParentOverflowed = false;
		// if the parent node will be overflowed
		if (parentNode.genreCount() + 1 > 2 * PlaylistNode.order) {
			// mark the parent overflow information as true
			willParentOverflowed = true;
		}

		// create a new leaf instead of overflowed leaf
		PlaylistNodeSecondaryLeaf newOverflowedLeaf = new PlaylistNodeSecondaryLeaf(parentNode);
		// create a new leaf for the right side of the overflowed leaf which has the same parent
		PlaylistNodeSecondaryLeaf newLeaf = new PlaylistNodeSecondaryLeaf(parentNode);


		// get index of the overflowed leaf in the children of the parent
		int overflowedLeafIndexAsChild = getIndexToInsert(parentNode, overflowedLeaf.genreAtIndex(0));
		// remove overflowed leaf from the children of the parent
		parentNode.removeChild(overflowedLeafIndexAsChild);

		// get the song id of the song located at the middle of the overflowed leaf
		int middleIndex = songCountOfOverflowedLeaf / 2;

		// move the right elements of the overflowed leaf to the new leaf, and left elements of the overflowed leaf to the new overflowed leaf
		for (int i = 0; i < songCountOfOverflowedLeaf; i++) {
			// if we are on the left side of the overflowed leaf
			if (i < middleIndex) {
				// copy the songs to the new overflowed leaf
				newOverflowedLeaf.getSongBucket().add(i, overflowedLeafSongs.get(i));
			}
			else if (i == middleIndex) {
				// get index to copy up the middle element
				if (overflowedLeafSongs.get(i).size() == 0) {
					System.out.println("ERROR 1");
					return;
				}
				int indexToCopyUp = getIndexToInsert(parentNode, overflowedLeafSongs.get(i).get(0).genre());
				// copy up the middle element to the parent node (into the correct place)
				parentGenres.add(indexToCopyUp, overflowedLeafSongs.get(i).get(0).genre()); // parentNode.addAudioId(indexToCopyUp, overflowedLeafSongs.get(i).audioId());
				// copy the song to the new leaf
				newLeaf.getSongBucket().add(i - middleIndex, overflowedLeafSongs.get(i));
			}
			// if we are on the right side of the overflowed leaf
			else {
				// copy the songs to the new leaf
				newLeaf.getSongBucket().add(i - middleIndex, overflowedLeafSongs.get(i));
			}
		}

		// if there will be no overflow in the parent node
		if (!willParentOverflowed) {
			// update the parent node's audio id's
			parentNode.setGenres(parentGenres);
			// insert the new leaf as child
			parentNode.addChild(overflowedLeafIndexAsChild, newLeaf);
			// insert the new overflowed leaf as child
			parentNode.addChild(overflowedLeafIndexAsChild, newOverflowedLeaf);
			// terminate the process
			return;
		}

		// if there will be an overflow in the parent node
		// test
		ArrayList<PlaylistNode> overflowedNodeChildren = new ArrayList<PlaylistNode>(parentNode.getAllChildren());
		overflowedNodeChildren.add(overflowedLeafIndexAsChild, newLeaf);
		overflowedNodeChildren.add(overflowedLeafIndexAsChild, newOverflowedLeaf);
		// test

		handleOverflowFromNode(parentNode, parentGenres, overflowedNodeChildren);
		// some code here...


	}


	private void handleOverflowFromNode(PlaylistNodeSecondaryIndex overFlowedNode, ArrayList<String> overflowedGenres, ArrayList<PlaylistNode> overflowedNodeChildren) {

		// base case
		// if this is the primary root
		if ((PlaylistNodeSecondaryIndex) overFlowedNode.getParent() == null) {
			// create a new parent node
			PlaylistNodeSecondaryIndex newParentNode = new PlaylistNodeSecondaryIndex(null);
			// create a new overflowed node for the left side of the overflowed node
			PlaylistNodeSecondaryIndex newOverflowedNode = new PlaylistNodeSecondaryIndex(newParentNode);
			// create a new node for the right side of the overflowed node
			PlaylistNodeSecondaryIndex newNode = new PlaylistNodeSecondaryIndex(newParentNode);
			// set new overflowed node and new node as the children of the new parent node
			newParentNode.addChild(newOverflowedNode);
			newParentNode.addChild(newNode);
			// move up the middle element to the new parent node
			int middleIndex = overflowedGenres.size() / 2;
			newParentNode.addGenre(overflowedGenres.get(middleIndex));
			// move the right audio id's of the overflowed node
			int i;
			for (i = 0; i < overflowedGenres.size(); i++) {
				if (i < middleIndex) {
					// add the audio id to the new overflowed node audio id's
					newOverflowedNode.addGenre(overflowedGenres.get(i));
					// add the children to the overflowed node children
					newOverflowedNode.addChild(overflowedNodeChildren.get(i));
					// update the parent of the children to the new overflowed node
					overflowedNodeChildren.get(i).setParent(newOverflowedNode);
				}
				else if (i == middleIndex) {
					// add the children to the overflowed node children
					newOverflowedNode.addChild(overflowedNodeChildren.get(i));
					// update the parent of the children to the new overflowed node
					overflowedNodeChildren.get(i).setParent(newOverflowedNode);
				}
				else {
					// add the audio id to the new node audio id's
					newNode.addGenre(overflowedGenres.get(i));
					// add the children to the new node children
					newNode.addChild(overflowedNodeChildren.get(i));
					// update the parent of the children to the new node
					overflowedNodeChildren.get(i).setParent(newNode);
				}
			}
			// add the last child to the new node children
			newNode.addChild(overflowedNodeChildren.get(i));
			// update the parent of the children to the new node
			overflowedNodeChildren.get(i).setParent(newNode);
			// remove the audio id
			overflowedGenres.remove(middleIndex);

			// update primary root such that it is new parent node
			this.secondaryRoot = newParentNode;

			// terminate the process
			return;
		}



		// recursive case
		// get parent of the overflowed node
		PlaylistNodeSecondaryIndex parentNode = (PlaylistNodeSecondaryIndex) overFlowedNode.getParent();

		// if an overflow will occur in the parent node of the overflowed node
		boolean willParentOverflowed = false;
		// if the parent node will be overflowed
		if (parentNode.genreCount() + 1 > 2 * PlaylistNode.order) {
			// mark the parent overflow information as true
			willParentOverflowed = true;
		}
		// burada tekrar eden genre alamazsın. Çünkü genre tekrar ettiğinde asla overflow method'una girmiyoruz zaten.

		// get the keys of the parent node
		ArrayList<String> parentNodeGenres = new ArrayList<String>(parentNode.getAllGenres());
		// get the children of the parent node
		ArrayList<PlaylistNode> parentNodeChildren = new ArrayList<PlaylistNode>(parentNode.getAllChildren());

		// create a new left node for the left side of the overflowed node
		PlaylistNodeSecondaryIndex newLeftNode = new PlaylistNodeSecondaryIndex(parentNode);
		// create a new node for the right side of the overflowed node
		PlaylistNodeSecondaryIndex newRightNode = new PlaylistNodeSecondaryIndex(parentNode);

		// get the index of the overflowed node as the child of the parent node
		int overflowedNodeIndexAsChild = getIndexToInsert(parentNode, overFlowedNode.genreAtIndex(0));
		// remove the overflowed node from parent node children
		parentNodeChildren.remove(overflowedNodeIndexAsChild);
		// insert the new right node as a child of the parent
		parentNodeChildren.add(overflowedNodeIndexAsChild, newRightNode);
		// insert the new left node as a child of the parent
		parentNodeChildren.add(overflowedNodeIndexAsChild, newLeftNode);

		// get the middle index
		int middleIndex = overflowedGenres.size() / 2;

		int i;
		for (i = 0; i < overflowedGenres.size(); i++) {
			if (i < middleIndex) {
				// add the key to the new left node keys
				newLeftNode.addGenre(overflowedGenres.get(i));
				// add the children to the overflowed node children
				newLeftNode.addChild(overflowedNodeChildren.get(i));
				// update the parent of the children to the new overflowed node
//				System.out.println("#########");
//				for (int j = 0; j < newLeftNode.audioIdCount(); j++) {
//					System.out.println(newLeftNode.audioIdAtIndex(i));
//				}
//				System.out.println("#########");
				overflowedNodeChildren.get(i).setParent(newLeftNode);
			}
			else if (i == middleIndex) {
				// add the children to the overflowed node children
				newLeftNode.addChild(overflowedNodeChildren.get(i));
				// update the parent of the children to the new left node
//				System.out.println("#########");
//				for (int j = 0; j < newLeftNode.audioIdCount(); j++) {
//					System.out.println(newLeftNode.audioIdAtIndex(i));
//				}
//				System.out.println("#########");
				overflowedNodeChildren.get(i).setParent(newLeftNode);
			}
			else {
				// add the audio id to the new right node audio id's
				newRightNode.addGenre(overflowedGenres.get(i));
				// add the children to the new node children
				newRightNode.addChild(overflowedNodeChildren.get(i));
				// update the parent of the children to the new node
//				System.out.println("#########");
//				for (int j = 0; j < newRightNode.audioIdCount(); j++) {
//					System.out.println(newRightNode.audioIdAtIndex(i));
//				}
//				System.out.println("#########");
				overflowedNodeChildren.get(i).setParent(newRightNode);
			}
		}
		// add the last child to the new node children
		newRightNode.addChild(overflowedNodeChildren.get(i));
		// update the parent of the children to the new right node
//		System.out.println("#########");
//		for (int j = 0; j < newRightNode.audioIdCount(); j++) {
//			System.out.println(newRightNode.audioIdAtIndex(i));
//		}
//		System.out.println("#########");
		overflowedNodeChildren.get(i).setParent(newRightNode);

		// find the correct index to move up the middle element
		int middleElementInsertionIndex = getIndexToInsert(parentNode, overflowedGenres.get(middleIndex));
		// move up the middle element to the new parent node
		parentNodeGenres.add(middleElementInsertionIndex, overflowedGenres.get(middleIndex));
		// remove the middle key
		overflowedGenres.remove(middleIndex);

		// if no overflow will occur
		if (!willParentOverflowed) {
			parentNode.setGenres(parentNodeGenres);
			parentNode.setChildren(parentNodeChildren);
			return;
		}

		ArrayList<String> parentNodeKeysCopy = new ArrayList<String>(parentNodeGenres);
		ArrayList<PlaylistNode> parentNodeChildrenCopy = new ArrayList<PlaylistNode>(parentNodeChildren);

		// if an overflow in the parent node will occur
		// DİKKKATTTTTTT! BURADA IF STATEMENT OLMASI GEREKEMEZ MI?
		handleOverflowFromNode(parentNode, parentNodeKeysCopy, parentNodeChildrenCopy);

		parentNode.setGenres(parentNodeGenres);
		parentNode.setChildren(parentNodeChildren);
		return;

	}





























	private void putIndentation(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("\t");
		}
	}

	private void printWithIndentation(String message, int level) {
		putIndentation(level);
		System.out.println(message);
	}


	private CengSong searchSongStartingFromNode(PlaylistNode node, Integer audioId, int indentationLevel) {
		// base case (if this a leaf node)
		if (node.type == PlaylistNodeType.Leaf) {
			// loop over all songs of the leaf
			for (int i = 0; i < ((PlaylistNodePrimaryLeaf) node).songCount(); i++) {
				// if the current song has audioId equal to given audioId
				if (((PlaylistNodePrimaryLeaf) node).audioIdAtIndex(i) == audioId) {
					// then, this is the song we are looking for
					// get the song
					CengSong foundedSong = ((PlaylistNodePrimaryLeaf) node).songAtIndex(i);
					// print the song
					putIndentation(indentationLevel);
					System.out.println("<data>");
					putIndentation(indentationLevel);
					System.out.print("<record>");
					System.out.print("" + foundedSong.audioId() + "|" + foundedSong.genre() + "|" + foundedSong.songName() + "|" + foundedSong.artist());
					System.out.println("</record>");
					putIndentation(indentationLevel);
					System.out.println("</data>");
					return ((PlaylistNodePrimaryLeaf) node).songAtIndex(i);
				}
			}
			// at this point, the song does not exist in the leaf
			// print the message
			System.out.println("Could not find " + audioId);
			// return null
			return null;
		}
		// recursive case (if this is an internal node)
		// print the node
		putIndentation(indentationLevel);
		System.out.println("<index>");
		for (int i = 0; i < ((PlaylistNodePrimaryIndex) node).audioIdCount(); i++) {
			putIndentation(indentationLevel);
			System.out.println(((PlaylistNodePrimaryIndex) node).audioIdAtIndex(i));
		}
		putIndentation(indentationLevel);
		System.out.println("</index>");
		// find the correct index to continue with
		for (int i = 0; i < ((PlaylistNodePrimaryIndex) node).audioIdCount(); i++) {
			// if the given audio id is less than the current audio id in the node
			if (audioId < ((PlaylistNodePrimaryIndex) node).audioIdAtIndex(i)) {
				// continue with that child
				return searchSongStartingFromNode(((PlaylistNodePrimaryIndex) node).getChildrenAt(i), audioId, indentationLevel + 1);
			}
		}
		// if the given audio id is greater than or equal to all audio ids in the node
		// then, continue with the last children
		return searchSongStartingFromNode( ((PlaylistNodePrimaryIndex) node).getChildrenAt(((PlaylistNodePrimaryIndex) node).audioIdCount()) , audioId, indentationLevel + 1);
	}

	public CengSong searchSong(Integer audioId) {
		// TODO: Implement this method
		// find the song with the searched audioId in primary B+ tree
		// return value will not be tested, just print according to the specifications
		return searchSongStartingFromNode(this.primaryRoot, audioId, 0);
	}
	

	private void printPrimaryPlaylistFromNode(PlaylistNode node, int indentationLevel) {
		// base case (if this is a leaf node)
		if (node.type == PlaylistNodeType.Leaf) {
			// print the leaf node
			printWithIndentation("<data>", indentationLevel);

			for (int i = 0; i < ((PlaylistNodePrimaryLeaf) node).songCount(); i++) {
				// get the current song in the leaf
				CengSong currentSong = ((PlaylistNodePrimaryLeaf) node).songAtIndex(i);

				// print the song
				putIndentation(indentationLevel);
				System.out.print("<record>");

				System.out.print("" + currentSong.audioId() + "|" + currentSong.genre() + "|" + currentSong.songName() + "|" + currentSong.artist());

				System.out.println("</record>");
			}

			printWithIndentation("</data>", indentationLevel);
			return;
		}
		// recursive case (if this is an internal node)

		// print the node
		printWithIndentation("<index>", indentationLevel);
		for (int i = 0; i < ((PlaylistNodePrimaryIndex) node).audioIdCount(); i++) {
			printWithIndentation(((PlaylistNodePrimaryIndex) node).audioIdAtIndex(i).toString(), indentationLevel);
		}
		printWithIndentation("</index>", indentationLevel);

		// print the children of the node (loop over all children of the node)
		for (int i = 0; i < ((PlaylistNodePrimaryIndex) node).getChildrenCount(); i++) {
			printPrimaryPlaylistFromNode(((PlaylistNodePrimaryIndex) node).getChildrenAt(i), indentationLevel + 1);
		}
	}

	public void printPrimaryPlaylist() {
		// TODO: Implement this method
		// print the primary B+ tree in Depth-first order
		printPrimaryPlaylistFromNode(this.primaryRoot, 0);
		return;
	}

	private void printSecondaryPlaylistFromNode(PlaylistNode node, int indentationLevel) {
		// base case (if the node is a leaf)
		if (node.type == PlaylistNodeType.Leaf) {
			// print the leaf node
			printWithIndentation("<data>", indentationLevel);

			for (int i = 0; i < ((PlaylistNodeSecondaryLeaf) node).genreCount(); i++) {
				// get the current genre songs
				ArrayList<CengSong> genreSongs = ((PlaylistNodeSecondaryLeaf) node).getSongBucket().get(i);

				// print the name of the genre
				printWithIndentation(((PlaylistNodeSecondaryLeaf) node).genreAtIndex(i), indentationLevel);

				// print the genre songs
				for (int j = 0; j < genreSongs.size(); j++) {
					// get the current song
					CengSong currentSong = genreSongs.get(j);
					// print the song
					putIndentation(indentationLevel + 1);
					System.out.print("<record>");
					System.out.print("" + currentSong.audioId() + "|" + currentSong.genre() + "|" + currentSong.songName() + "|" + currentSong.artist());
					System.out.println("</record>");
				}
			}

			printWithIndentation("</data>", indentationLevel);

			return;
		}
		// recursive case (if the node is an internal node)

		// print the node
		printWithIndentation("<index>", indentationLevel);
		for (int i = 0; i < ((PlaylistNodeSecondaryIndex) node).genreCount(); i++) {
			printWithIndentation(((PlaylistNodeSecondaryIndex) node).genreAtIndex(i), indentationLevel);
		}
		printWithIndentation("</index>", indentationLevel);

		// print the children of the node (loop over all children of the node)
		for (int i = 0; i < ((PlaylistNodeSecondaryIndex) node).getChildrenCount(); i++) {
			printSecondaryPlaylistFromNode(((PlaylistNodeSecondaryIndex) node).getChildrenAt(i), indentationLevel + 1);
		}
	}

	public void printSecondaryPlaylist() {
		// TODO: Implement this method
		// print the secondary B+ tree in Depth-first order
		printSecondaryPlaylistFromNode(this.secondaryRoot, 0);
		return;
	}
	
	// Extra functions if needed

}