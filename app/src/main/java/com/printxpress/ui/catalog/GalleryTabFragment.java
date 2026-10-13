package com.printxpress.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.printxpress.R;
import com.printxpress.data.entity.SampleDesign;
import com.printxpress.data.repository.CatalogRepository;
import com.printxpress.util.GenericAdapter;

public class GalleryTabFragment extends Fragment {
    private CatalogRepository catalogRepository;
    private GenericAdapter<SampleDesign> galleryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        catalogRepository = new CatalogRepository(requireActivity().getApplication());

        RecyclerView rv = view.findViewById(R.id.rvGalleryGrid);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));

        galleryAdapter = new GenericAdapter<>(R.layout.item_gallery_grid,
            (v, design) -> {
                TextView tvTitle = v.findViewById(R.id.tvGalleryTitle);
                ImageView ivThumb = v.findViewById(R.id.ivGalleryThumb);
                tvTitle.setText(design.title);
                ivThumb.setImageResource(design.imageRes);
            },
            this::showFullScreenPreview
        );
        rv.setAdapter(galleryAdapter);

        catalogRepository.getSamplesByCategory(1).observe(getViewLifecycleOwner(), designs -> {
            if (designs != null) {
                galleryAdapter.setItems(designs);
            }
        });
    }

    private void showFullScreenPreview(SampleDesign design) {
        new AlertDialog.Builder(requireContext())
            .setTitle(design.title)
            .setMessage("Preview of professional template design.")
            .setPositiveButton("Customize Template", (dialog, which) -> {
                Bundle bundle = new Bundle();
                bundle.putString("templateTitle", design.title);
                bundle.putLong("categoryId", design.categoryId);
                Navigation.findNavController(requireView()).navigate(R.id.universalTemplateEditorFragment, bundle);
            })
            .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
            .show();
    }
}
