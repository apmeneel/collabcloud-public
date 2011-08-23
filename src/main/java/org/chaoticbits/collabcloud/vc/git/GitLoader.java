package org.chaoticbits.collabcloud.vc.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.chaoticbits.collabcloud.Developer;
import org.chaoticbits.collabcloud.codeprocessor.CloudWeights;
import org.chaoticbits.collabcloud.codeprocessor.ISummarizable;
import org.chaoticbits.collabcloud.codeprocessor.java.JavaClassArtifact;
import org.chaoticbits.collabcloud.vc.IVersionControlLoader;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitLoader implements IVersionControlLoader {

	private static final double IN_DIFF_MULTIPLIER = 2.0;
	

	private FileRepository repo;
	private ObjectId since;

	public GitLoader(File repoDir) throws IOException {
		repo = new FileRepositoryBuilder().setGitDir(repoDir).readEnvironment().findGitDir().build();
	}

	public Set<Developer> getDevelopers() {
		checkSince();
		Set<Developer> set = new HashSet<Developer>();
		RevWalk rw = loadRevWalk();
		Iterator<RevCommit> itr = rw.iterator();
		while (itr.hasNext()) {
			RevCommit next = itr.next();
			set.add(new Developer(next.getAuthorIdent().getName(), next.getAuthorIdent().getEmailAddress()));
		}
		return set;
	}

	private void checkSince() {
		if (since == null)
			throw new IllegalAccessError("Mark the since variable first.");
	}

	public Set<ISummarizable> getFilesChanged() throws IOException {
		checkSince();
		return extractFiles(buildDiffString(), new HashSet<ISummarizable>());
	}

	private String buildDiffString() throws IOException {
		RevWalk rw = loadRevWalk();
		StringBuilder builder = new StringBuilder();
		Iterator<RevCommit> itr = rw.iterator();
		while (itr.hasNext()) {
			RevCommit commit = itr.next();
			RevCommit parent = commit.getParent(0); // TODO Handle multiple parents
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DiffFormatter formatter = new DiffFormatter(out);
			formatter.setRepository(repo);
			formatter.format(commit, parent);
			builder.append(out.toString());
		}
		return builder.toString();
	}

	private Set<ISummarizable> extractFiles(String diffsString, Set<ISummarizable> set) {
		Scanner scanner = new Scanner(diffsString);
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			if (line.startsWith("+++") || line.startsWith("---")) {
				set.add(new JavaClassArtifact(line.substring(6)));
			}
		}
		return set;
	}

	private RevWalk loadRevWalk() {
		RevWalk rw = new RevWalk(repo);
		try {
			rw.markStart(rw.parseCommit(repo.resolve("HEAD")));
			rw.markUninteresting(rw.parseCommit(since));
		} catch (MissingObjectException e) {
			System.err.println("Error loading git repo.");
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			System.err.println("Error loading git repo.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error loading git repo.");
			e.printStackTrace();
		}
		return rw;
	}

	public FileRepository getRepo() {
		return repo;
	}

	public void markSince(ObjectId since) {
		if (since != null)
			this.since = since;
	}

	public ObjectId getSince() {
		return since;
	}

	public CloudWeights crossWithDiff(CloudWeights weights) throws IOException {
		String diffsString = buildDiffString();
		Scanner scanner = new Scanner(diffsString);
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			Set<Entry<String, Double>> unsortedEntries = weights.unsortedEntries();
			for (Entry<String, Double> entry : unsortedEntries) {
				if (line.contains(entry.getKey())) {
					weights.multiply(entry.getKey(), IN_DIFF_MULTIPLIER);
				}
			}
		}
		return weights;
	}

}